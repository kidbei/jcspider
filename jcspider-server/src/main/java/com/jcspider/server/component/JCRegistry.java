package com.jcspider.server.component;

import java.util.List;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public interface JCRegistry extends JCComponent {

    void registerDispatcher(String host);

    void registerProcess(String host);

    List<String> listDispatchers();

}
