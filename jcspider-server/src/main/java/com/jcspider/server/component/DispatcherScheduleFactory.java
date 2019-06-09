package com.jcspider.server.component;

import com.jcspider.server.utils.Constant;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public class DispatcherScheduleFactory {

    private static ScheduledThreadPoolExecutor schedulePool;

    private static final Map<Long, ProjectDispatcherRunner> PROJECT_DISPATCHER_RUNNER_MAP = new ConcurrentHashMap<>();


    public static synchronized void init(int maxScheduleSize) {
        if (schedulePool == null) {
            schedulePool = new ScheduledThreadPoolExecutor(maxScheduleSize, r -> {
                Thread t = new Thread(r);
                t.setName("dispatcher_schedule_" + t.getId());
                return t;
            });
        } else {
            throw new IllegalStateException("schedule factory is already init");
        }
    }



    public static void setProjectRunner(long projectId, ProjectDispatcherRunner runner, String rateUnit, int rateUnitMultiple) {
        PROJECT_DISPATCHER_RUNNER_MAP.put(projectId, runner);
        TimeUnit timeUnit;
        if (Constant.UNIT_TYPE_SECONDS.equals(rateUnit)) {
            timeUnit = TimeUnit.SECONDS;
        } else if (Constant.UNIT_TYPE_MINUTES.equals(rateUnit)) {
            timeUnit = TimeUnit.MINUTES;
        } else {
            throw new IllegalArgumentException("not support rate unit:" + rateUnit);
        }
        long delay = rateUnitMultiple == 0 ? 1L : rateUnitMultiple;
        schedulePool.scheduleWithFixedDelay(runner, 0L, rateUnitMultiple, timeUnit);
    }


    public static void stopProjectRunner(long projectId) {
        ProjectDispatcherRunner runner = PROJECT_DISPATCHER_RUNNER_MAP.remove(projectId);
        if (runner != null) {
            runner.setStop(true);
        }
    }


    public static boolean isDispatcherStarted(long projectId) {
        return PROJECT_DISPATCHER_RUNNER_MAP.containsKey(projectId);
    }
}
