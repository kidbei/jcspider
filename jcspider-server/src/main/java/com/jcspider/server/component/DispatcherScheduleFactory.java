package com.jcspider.server.component;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public class DispatcherScheduleFactory {

    private static ScheduledThreadPoolExecutor schedulePool;


    public static synchronized void init(int maxScheduleSize) {
        if (schedulePool == null) {
            schedulePool = new ScheduledThreadPoolExecutor(maxScheduleSize, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("dispatcher_schedule_" + t.getId());
                    return t;
                }
            });
        } else {
            throw new IllegalStateException("schedule factory is already init");
        }
    }





}
