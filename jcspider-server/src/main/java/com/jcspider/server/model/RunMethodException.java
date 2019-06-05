package com.jcspider.server.model;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
public class RunMethodException extends Exception {

    private String method;


    public RunMethodException(String method) {
        this.method = method;
    }

    public RunMethodException(String message, String method) {
        super(message);
        this.method = method;
    }

    public RunMethodException(String message, Throwable cause, String method) {
        super(message, cause);
        this.method = method;
    }

    public RunMethodException(Throwable cause, String method) {
        super(cause);
        this.method = method;
    }

    public RunMethodException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String method) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.method = method;
    }
}
