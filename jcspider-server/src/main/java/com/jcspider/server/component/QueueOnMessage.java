package com.jcspider.server.component;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public interface QueueOnMessage {

    void onMessage(String topic, Object message);


}
