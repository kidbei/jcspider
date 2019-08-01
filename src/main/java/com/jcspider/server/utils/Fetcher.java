package com.jcspider.server.utils;

import java.io.IOException;

import com.jcspider.server.model.FetchResult;
import com.jcspider.server.model.SimpleTask;

/**
 * @author zhuang.hu
 * @since 04 June 2019
 */
public interface Fetcher {


    FetchResult fetch(SimpleTask task) throws IOException;

    void shutdown();

}
