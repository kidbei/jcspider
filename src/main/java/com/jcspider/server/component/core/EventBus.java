package com.jcspider.server.component.core;

import com.google.common.collect.ArrayListMultimap;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuang.hu Date:2019-09-09 Time:14:17
 */
public class EventBus {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventBus.class);


    private static final ArrayListMultimap<String, OnEvent> eventMap = ArrayListMultimap.create();

    private static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(1, 1, Integer.MAX_VALUE, TimeUnit.HOURS, new LinkedBlockingQueue<>());

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(()->poolExecutor.shutdown()));
    }


    public synchronized static void consume(String topic, OnEvent onEvent) {
        eventMap.put(topic, onEvent);
    }


    public synchronized static void unregister(String topic, OnEvent onEvent) {
        eventMap.remove(topic, onEvent);
    }

    public static void produce(String topic, Object value) {
        poolExecutor.execute(() -> {
            synchronized (eventMap) {
                List<OnEvent> onEventList = eventMap.get(topic);
                if (CollectionUtils.isNotEmpty(onEventList)) {
                    onEventList.forEach(onEvent -> {
                        try {
                            onEvent.event(topic, value);
                        } catch (Exception e) {
                            LOGGER.error("process event error", e);
                        }
                    });
                }
            }
        });
    }
}
