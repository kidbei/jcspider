package com.jcspider.server.model;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.Map;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class Response {

    private Map<String, String> headers;
    private String  content;

    public Response(Map<String, String> headers, String content) {
        this.headers = headers;
        this.content = content;
    }

    public Response() {
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Elements doc(String selector) {
        return Jsoup.parse(this.content).select(selector);
    }
}
