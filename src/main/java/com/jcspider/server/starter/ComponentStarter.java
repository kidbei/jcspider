package com.jcspider.server.starter;

import com.jcspider.server.model.ComponentInitException;
import com.jcspider.server.component.JCComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
@Component
public class ComponentStarter implements ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentStarter.class);

    private Map<String, JCComponent> componentMap;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        componentMap = applicationContext.getBeansOfType(JCComponent.class);
        componentMap.values().forEach(jcComponent -> {
            try {
                LOGGER.info("start component {}", jcComponent.name());
                jcComponent.start();
                LOGGER.info("start component {} complete", jcComponent.name());
            } catch (ComponentInitException e) {
                LOGGER.error("start component {} error", jcComponent.name(), e);
                throw new RuntimeException(e);
            }
        });
    }

    @PreDestroy
    public void shutdown() {
        this.componentMap.values().forEach(jcComponent -> {
            try {
                jcComponent.shutdown();
            } catch (Exception e) {
                LOGGER.error("shutdown component {} error", jcComponent.name(), e);
            }
        });
    }
}
