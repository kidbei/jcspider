package com.jcspider.server.starter;

import com.jcspider.server.component.*;
import com.jcspider.server.utils.Constant;
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

import java.util.Arrays;
import java.util.List;

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
        final boolean processEnable = Boolean.valueOf(env.getProperty("process.enable", "true"));
        final String  processModel = env.getProperty("process.model", "nashorn");
        final String  model = env.getProperty("model");
        final String  resultExporter = env.getProperty("process.result.exporter", "dbResultExporter");
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

            }

            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

                if (Constant.MODEL_LOCAL.equalsIgnoreCase(model)) {
                    LOGGER.info("current model is local, register JCLocalQueue, JCLocalRegistry");
                    registry.registerBeanDefinition("jcQueue",
                            BeanDefinitionBuilder.rootBeanDefinition(JCLocalQueue.class).getBeanDefinition());
                    registry.registerBeanDefinition("jcLocalRegistry",
                            BeanDefinitionBuilder.rootBeanDefinition(JCLocalRegistry.class).getBeanDefinition());
                    registry.registerBeanDefinition("jcLockTool",
                            BeanDefinitionBuilder.rootBeanDefinition(JCLocalLockTool.class).getBeanDefinition());
                } else if (Constant.MODEL_CLUSTER.equalsIgnoreCase(model)){
                    LOGGER.info("current model is cluster ");
                } else {
                    throw new BeanCreationException("invalid model:" + model);
                }

                if (dispatcherEnable) {
                    LOGGER.info("start model:jcDispatcher");
                    registry.registerBeanDefinition("jcDispatcher",
                            BeanDefinitionBuilder.rootBeanDefinition(JCDispatcher.class).getBeanDefinition());
                }
                if (processEnable) {
                    if (processModel.equals("nashorn")) {
                        registry.registerBeanDefinition("jSR223EngineProcess",
                                BeanDefinitionBuilder.rootBeanDefinition(JSR223EngineProcess.class).getBeanDefinition());
                    }
                    List<String> exporterComponents = Arrays.asList(resultExporter.split(","));
                    if (exporterComponents.contains(Constant.DB_RESULT_EXPORTER)) {
                        registry.registerBeanDefinition(Constant.DB_RESULT_EXPORTER,
                                BeanDefinitionBuilder.rootBeanDefinition(DbResultExporter.class).getBeanDefinition());
                    }
                }

            }
        };
    }


}
