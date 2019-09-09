package com.jcspider.server.component.ifc;

import com.jcspider.server.model.ComponentInitException;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public interface JCComponent {

    void start() throws ComponentInitException;

    void shutdown();

    String name();

}
