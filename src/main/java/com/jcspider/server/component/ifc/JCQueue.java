package com.jcspider.server.component.ifc;

import com.jcspider.server.component.core.OnEvent;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public interface JCQueue extends JCComponent {

    void publish(String topic, Object value);

    void subscribe(String topic, OnEvent onEvent);

}
