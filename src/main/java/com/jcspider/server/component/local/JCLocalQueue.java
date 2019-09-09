package com.jcspider.server.component.local;

import com.jcspider.server.component.core.EventBus;
import com.jcspider.server.component.core.OnEvent;
import com.jcspider.server.component.ifc.JCQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuang.hu
 * @since 30 May 2019
 */
public class JCLocalQueue implements JCQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(JCLocalQueue.class);


    @Override
    public void publish(String topic, Object value) {
        EventBus.produce(topic, value);
    }

    @Override
    public void subscribe(String topic, OnEvent onEvent) {
        EventBus.consume(topic, onEvent);
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public String name() {
        return "localQueue";
    }
}
