package com.jcspider.server.starter;

import com.jcspider.server.component.DbResultExporter;
import com.jcspider.server.component.JCDispatcher;
import com.jcspider.server.component.JSR223EngineProcess;
import com.jcspider.server.component.local.JCLocalLockTool;
import com.jcspider.server.component.local.JCLocalQueue;
import com.jcspider.server.component.local.JCLocalRegistry;
import com.jcspider.server.component.redis.RedisLockTool;
import com.jcspider.server.component.redis.RedisQueue;
import com.jcspider.server.component.redis.RedisRegistry;
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
        final boolean dbExporterDisable = Boolean.valueOf(env.getProperty("process.dbExporter.disable", "false"));
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
                    registry.registerBeanDefinition("jcRegistry",
                            BeanDefinitionBuilder.rootBeanDefinition(JCLocalRegistry.class).getBeanDefinition());
                    registry.registerBeanDefinition("jcLockTool",
                            BeanDefinitionBuilder.rootBeanDefinition(JCLocalLockTool.class).getBeanDefinition());
                } else if (Constant.MODEL_CLUSTER.equalsIgnoreCase(model)){
                    LOGGER.info("current model is cluster ");
                    registry.registerBeanDefinition("jcQueue",
                            BeanDefinitionBuilder.rootBeanDefinition(RedisQueue.class).getBeanDefinition());
                    registry.registerBeanDefinition("jcRegistry",
                            BeanDefinitionBuilder.rootBeanDefinition(RedisRegistry.class).getBeanDefinition());
                    registry.registerBeanDefinition("jcLockTool",
                            BeanDefinitionBuilder.rootBeanDefinition(RedisLockTool.class).getBeanDefinition());
                } else {
                    throw new BeanCreationException("invalid model:" + model);
                }
                if (processEnable) {
                    if (processModel.equals("nashorn")) {
                        registry.registerBeanDefinition("jSR223EngineProcess",
                                BeanDefinitionBuilder.rootBeanDefinition(JSR223EngineProcess.class).getBeanDefinition());
                    }
                    List<String> exporterComponents = Arrays.asList(resultExporter.split(","));
                }
                if (dispatcherEnable) {
                    LOGGER.info("start model:jcDispatcher");
                    registry.registerBeanDefinition("jcDispatcher",
                            BeanDefinitionBuilder.rootBeanDefinition(JCDispatcher.class).getBeanDefinition());
                }
                if (!dbExporterDisable) {
                    registry.registerBeanDefinition(Constant.DB_RESULT_EXPORTER,
                            BeanDefinitionBuilder.rootBeanDefinition(DbResultExporter.class).getBeanDefinition());
                }
            }
        };
    }



}
