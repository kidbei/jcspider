package com.jcspider.server.model;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
public class RunMethodException extends Exception {

    private static final long serialVersionUID = -8195527833289848206L;

    public RunMethodException(String method) {
    }

    public RunMethodException(String message, String method) {
        super(message);
    }

    public RunMethodException(String message, Throwable cause, String method) {
        super(message, cause);
    }

    public RunMethodException(Throwable cause, String method) {
        super(cause);
    }

    public RunMethodException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace,
            String method) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
