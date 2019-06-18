package com.jcspider.server.utils;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.concurrent.TimeUnit;

/**
 * @author zhuang.hu
 * @since 18 June 2019
 */
public class WebDriverFactory extends BasePooledObjectFactory<WebDriver> {
    @Override
    public WebDriver create() throws Exception {
        return newDriver();
    }

    @Override
    public PooledObject<WebDriver> wrap(WebDriver webDriver) {
        return new DefaultPooledObject<>(webDriver);
    }


    private WebDriver newDriver() {
        DesiredCapabilities dCaps = new DesiredCapabilities();
        dCaps.setJavascriptEnabled(true);
        dCaps.setCapability("takesScreenshot", false);
        dCaps.setAcceptInsecureCerts(true);
        dCaps.setPlatform(Platform.LINUX);
        WebDriver webDriver = new PhantomJSDriver(dCaps);
        webDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
        return webDriver;
    }
}
