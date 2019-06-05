package com.jcspider.server.model;

import java.util.Map;

/**
 * @author zhuang.hu
 * @since 04 June 2019
 */
public class FetchResult {

    private String              content;
    private Map<String, String> headers;
    private int                 status;
    private boolean             success;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
