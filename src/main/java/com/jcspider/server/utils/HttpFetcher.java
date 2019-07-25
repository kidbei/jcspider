package com.jcspider.server.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jcspider.server.model.FetchResult;
import com.jcspider.server.model.SimpleTask;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhuang.hu
 * @since 04 June 2019
 */
public class HttpFetcher implements Fetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpFetcher.class);

    private static final String DEFAULT_UA = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:66.0) Gecko/20100101 Firefox/66.0";
    private static final String DEFAULT_CHARSET = "utf-8";
    private static final String UA_NAME = "User-Agent";


    @Override
    public FetchResult fetch(SimpleTask task) throws IOException{
        final String url = task.getSourceUrl();
        final FetchResult result = new FetchResult();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("http fetch, url:{}, task:{}", url, task);
        }

        final OkHttpClient httpClient = new OkHttpClient();
        final Request.Builder reqBuilder = new Request.Builder();

        Map<String,String> headers = this.getHeaders(task);
        if (MapUtils.isNotEmpty(headers)) {
            headers.forEach((k, v) -> reqBuilder.header(k, v));
        }
        if (!headers.containsKey(UA_NAME)) {
            reqBuilder.header(UA_NAME, DEFAULT_UA);
        }

        reqBuilder.url(url);
        reqBuilder.get();

        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.followRedirects(true);

        if (StringUtils.isNotBlank(task.getProxy())) {
            clientBuilder.proxy(new Proxy(Proxy.Type.SOCKS, this.parseProxyAddress(task)));
        }

        Response response = null;
        try {
            response = httpClient.newCall(reqBuilder.build()).execute();

            if (response.code() >= 400) {
                result.setStatus(response.code());
                result.setSuccess(false);
                return result;
            }

            LOGGER.info("fetch success, url:{}", url);

            final InputStream resultStream = response.body().byteStream();
            final String content = StringUtils.isBlank(task.getCharset()) ?
                    IOUtils.toString(resultStream, DEFAULT_CHARSET) : IOUtils.toString(resultStream, task.getCharset());

            result.setSuccess(true);
            result.setStatus(response.code());
            result.setContent(content);
            result.setHeaders(this.flatMapHeaders(response.headers()));
        } catch (Exception e) {
            LOGGER.error("request failed, url:{}", url, e);
            result.setSuccess(false);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return result;
    }


    private Map<String, String> flatMapHeaders(Headers headers) {
        Map<String, String> headerMap = new HashMap<>(headers.size());
        for (String name : headers.names()) {
            String value = headers.get(name);
            headerMap.put(name, value);
        }
        return headerMap;
    }



    private InetSocketAddress parseProxyAddress(SimpleTask task) {
        String proxy = task.getProxy();
        if (proxy.indexOf(":") == -1) {
            throw new IllegalArgumentException("invalid proxy address:" + task.getProxy());
        }
        String[] ss = proxy.split(":");
        return new InetSocketAddress(ss[0], Integer.valueOf(ss[1]));
    }


    private Map<String, String> getHeaders(SimpleTask task) {
        Map<String, String> result = new HashMap<>();
        if (StringUtils.isNotBlank(task.getHeaders())) {
            JSONObject headers = JSON.parseObject(task.getHeaders());
            headers.forEach((k,v) -> result.put(k, v.toString()));
        }
        return result;
    }

}
