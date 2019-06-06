package com.jcspider.server.utils;

import com.jcspider.server.model.FetchResult;
import com.jcspider.server.model.SimpleTask;
import com.jcspider.server.model.Task;

import java.io.IOException;

/**
 * @author zhuang.hu
 * @since 04 June 2019
 */
public interface Fetcher {


    FetchResult fetch(SimpleTask task) throws IOException;


}
