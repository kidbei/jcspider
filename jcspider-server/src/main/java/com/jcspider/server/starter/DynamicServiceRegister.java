package com.jcspider.server.starter;

import com.jcspider.server.component.JCDispatcher;
import com.jcspider.server.component.JCLocalQueue;
import com.jcspider.server.component.JCLocalRegistry;
import com.jcspider.server.component.JCNashornProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author zhuang.hu
 * @since 15 March 2019
 */
@Configuration
public class DynamicServiceRegister {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicServiceRegister.class);

    @Bean
    public BeanDefinitionRegistryPostProcessor addServiceBean(Environment env) {
        final boolean dispatcherEnable = Boolean.valueOf(env.getProperty("dispatcher.enable", "true"));
        final String  processModel = env.getProperty("process.model", "nashorn");
        final String model = env.getProperty("model");
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

            }

            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

                if ("local".equalsIgnoreCase(model)) {
                    LOGGER.info("current model is local, register JCLocalQueue, JCLocalRegistry");
                    registry.registerBeanDefinition("jcQueue",
                            BeanDefinitionBuilder.rootBeanDefinition(JCLocalQueue.class).getBeanDefinition());
                    registry.registerBeanDefinition("jcLocalRegistry",
                            BeanDefinitionBuilder.rootBeanDefinition(JCLocalRegistry.class).getBeanDefinition());
                } else if ("cluster".equalsIgnoreCase(model)){
                    LOGGER.info("current model is cluster ");
                } else {
                    throw new BeanCreationException("invalid model:" + model);
                }

                if (dispatcherEnable) {
                    LOGGER.info("start model:jcDispatcher");
                    registry.registerBeanDefinition("jcDispatcher",
                            BeanDefinitionBuilder.rootBeanDefinition(JCDispatcher.class).getBeanDefinition());
                }
                if (processModel.equals("nashorn")) {
                    registry.registerBeanDefinition("jCNashornProcess",
                            BeanDefinitionBuilder.rootBeanDefinition(JCNashornProcess.class).getBeanDefinition());
                }
            }
        };
    }


}
