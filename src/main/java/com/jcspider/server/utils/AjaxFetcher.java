package com.jcspider.server.utils;

import com.jcspider.server.model.FetchResult;
import com.jcspider.server.model.SimpleTask;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openqa.selenium.WebDriver;

import java.io.IOException;

/**
 * @author zhuang.hu
 * @since 18 June 2019
 */
public class AjaxFetcher implements Fetcher{

    private int getDriverTimeout = 1000 * 60;
    private int maxDriver = 10;
    private WebDriverPool pool;

    public AjaxFetcher() {
        WebDriverFactory factory = new WebDriverFactory();
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(maxDriver);
        config.setMaxWaitMillis(getDriverTimeout);
        this.pool = new WebDriverPool(factory, config);
    }

    @Override
    public FetchResult fetch(SimpleTask task) throws IOException {
        WebDriver webDriver;
        try {
            webDriver = this.pool.borrowObject();
        } catch (Exception e) {
            throw new IOException("can not get webdriver instance");
        }
        if (StringUtils.isNotBlank(task.getHeaders())) {
           //TODO set cookie
        }
       try {
           webDriver.get(task.getSourceUrl());
           String content = webDriver.getPageSource();
           FetchResult result = new FetchResult();
           result.setSuccess(true);
           result.setContent(content);
           result.setStatus(200);
           return result;
       } finally {
           webDriver.manage().deleteAllCookies();
       }
    }


    public int getGetDriverTimeout() {
        return getDriverTimeout;
    }

    public void setGetDriverTimeout(int getDriverTimeout) {
        this.getDriverTimeout = getDriverTimeout;
    }

    public int getMaxDriver() {
        return maxDriver;
    }

    public void setMaxDriver(int maxDriver) {
        this.maxDriver = maxDriver;
    }
}
