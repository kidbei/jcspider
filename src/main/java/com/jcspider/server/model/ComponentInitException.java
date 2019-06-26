package com.jcspider.server.model;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public class ComponentInitException extends Exception {

    private static final long serialVersionUID = -5038930924523396731L;
    
    public ComponentInitException(String componentName) {
    }

    public ComponentInitException(String message, String componentName) {
        super(message);
    }

    public ComponentInitException(String message, Throwable cause, String componentName) {
        super(message, cause);
    }

    public ComponentInitException(Throwable cause, String componentName) {
        super(cause);
    }

    public ComponentInitException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace, String componentName) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
