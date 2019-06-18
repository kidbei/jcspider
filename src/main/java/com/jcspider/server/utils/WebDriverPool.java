package com.jcspider.server.utils;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.WebDriver;

/**
 * @author zhuang.hu
 * @since 18 June 2019
 */
public class WebDriverPool extends GenericObjectPool<WebDriver> {
    public WebDriverPool(PooledObjectFactory<WebDriver> factory) {
        super(factory);
    }

    public WebDriverPool(PooledObjectFactory<WebDriver> factory, GenericObjectPoolConfig<WebDriver> config) {
        super(factory, config);
    }

    public WebDriverPool(PooledObjectFactory<WebDriver> factory, GenericObjectPoolConfig<WebDriver> config, AbandonedConfig abandonedConfig) {
        super(factory, config, abandonedConfig);
    }
}
