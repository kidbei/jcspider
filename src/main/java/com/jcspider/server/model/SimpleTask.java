package com.jcspider.server.model;

import java.io.Serializable;

/**
 * @author zhuang.hu
 * @since 06 June 2019
 */
public class SimpleTask implements Serializable {

    private static  final long serialVersionUID = 8059376589343829631L;
    private String  id;
    private String  method;
    private String  sourceUrl;
    private String  headers;
    private String  extra;
    private String  fetchType;
    private String  proxy;
    private String  charset;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getFetchType() {
        return fetchType;
    }

    public void setFetchType(String fetchType) {
        this.fetchType = fetchType;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
