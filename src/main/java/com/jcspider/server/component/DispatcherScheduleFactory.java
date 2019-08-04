package com.jcspider.server.component;

import com.jcspider.server.utils.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuang.hu
 * @since 29 May 2019
 */
public class DispatcherScheduleFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DispatcherScheduleFactory.class);

    private static ScheduledThreadPoolExecutor schedulePool;

    private static final Map<Long, ProjectDispatcherRunner> PROJECT_DISPATCHER_RUNNER_MAP = new ConcurrentHashMap<>();
    private static final Map<Long, ProjectDispatcherLoopRunner> PROJECT_DISPATCHER_LOOP_RUNNER_MAP = new ConcurrentHashMap<>();


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
        if (PROJECT_DISPATCHER_RUNNER_MAP.containsKey(projectId)) {
            LOGGER.warn("project {} is already in runner", projectId);
            return;
        }
        PROJECT_DISPATCHER_RUNNER_MAP.put(projectId, runner);
        TimeUnit timeUnit;
        if (Constant.UNIT_TYPE_SECONDS.equals(rateUnit)) {
            timeUnit = TimeUnit.SECONDS;
        } else if (Constant.UNIT_TYPE_MINUTES.equals(rateUnit)) {
            timeUnit = TimeUnit.MINUTES;
        } else if (Constant.UNIT_TYPE_HOURS.equals(rateUnit)) {
            timeUnit = TimeUnit.HOURS;
        } else {
            throw new IllegalArgumentException("not support rate unit:" + rateUnit);
        }
        long delay = rateUnitMultiple == 0 ? 1L : rateUnitMultiple;
        schedulePool.scheduleWithFixedDelay(runner, 0L, delay, timeUnit);
    }


    public static void stopProjectRunner(long projectId) {
        ProjectDispatcherRunner runner = PROJECT_DISPATCHER_RUNNER_MAP.remove(projectId);
        if (runner != null) {
            runner.setStop(true);
        }
    }

    public synchronized static final void setProjectDispatcherLoopRunner(long projectId, long scheduleValue) {
        LOGGER.info("set dispatcher loop,project:{}, scheduleValue:{}", projectId, scheduleValue);
        ProjectDispatcherLoopRunner loopRunner = new ProjectDispatcherLoopRunner(projectId);
        schedulePool.scheduleWithFixedDelay(loopRunner, 0L, scheduleValue, TimeUnit.MILLISECONDS);
        PROJECT_DISPATCHER_LOOP_RUNNER_MAP.put(projectId, loopRunner);
    }


    public static boolean isDispatcherStarted(long projectId) {
        return PROJECT_DISPATCHER_RUNNER_MAP.containsKey(projectId);
    }
}
