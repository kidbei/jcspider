package com.jcspider.server.component;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public interface JCComponent {

    void start() throws ComponentInitException;

    void shutdown();

    String name();

}
