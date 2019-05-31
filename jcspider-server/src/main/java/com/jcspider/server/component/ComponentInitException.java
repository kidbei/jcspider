package com.jcspider.server.component;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public class ComponentInitException extends Exception {

    private String componentName;

    public ComponentInitException(String componentName) {
        this.componentName = componentName;
    }

    public ComponentInitException(String message, String componentName) {
        super(message);
        this.componentName = componentName;
    }

    public ComponentInitException(String message, Throwable cause, String componentName) {
        super(message, cause);
        this.componentName = componentName;
    }

    public ComponentInitException(Throwable cause, String componentName) {
        super(cause);
        this.componentName = componentName;
    }

    public ComponentInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String componentName) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.componentName = componentName;
    }
}
