package com.jcspider;

import com.jcspider.server.model.SimpleTask;
import com.jcspider.server.utils.AjaxFetcher;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author zhuang.hu
 * @since 26 June 2019
 */
public class PhantomJSTest {


    @Test
    public void test() throws IOException {
        AjaxFetcher ajaxFetcher = new AjaxFetcher();
        SimpleTask task = new SimpleTask();
        task.setSourceUrl("http://www.baidu.com");
        Assert.assertNotNull(ajaxFetcher.fetch(task).getContent());
    }


}
