package com.jcspider.server.component.redis;

import com.jcspider.server.component.JCRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhuang.hu Date:2019-07-05 Time:16:57
 */
public class RedisRegistry implements JCRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRegistry.class);

    private static final String     DISPATCHER_KEY = "jc:dispatchers";
    private static final String     PROCESS_KEY = "jc:processes";
    private static final int        HEARTBEAT_TIME = 3;
    private static final int        HEARTBEAT_TIMEOUT_TIME = 30;


    private List<String>    dispatcherList = new ArrayList<>();
    private List<String>    processList = new ArrayList<>();

    private List<String>    localDispatcherList = new ArrayList<>();
    private List<String>    localProcessList = new ArrayList<>();

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;


    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void registerDispatcher(String host) {
        LOGGER.info("register dispatcher {}", host);
        this.redisTemplate.opsForZSet().add(DISPATCHER_KEY, host, System.currentTimeMillis());
        this.localDispatcherList.add(host);
    }

    @Override
    public void registerProcess(String host) {
        LOGGER.info("register process {}", host);
        this.redisTemplate.opsForZSet().add(PROCESS_KEY, host, System.currentTimeMillis());
        this.localProcessList.add(host);
    }

    @Override
    public List<String> listDispatchers() {
        return this.dispatcherList;
    }

    @Override
    public List<String> listProcesses() {
        return this.processList;
    }

    @Override
    public void start() {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2, r -> {
            Thread thread = new Thread(r);
            thread.setName("redis-register-schedule-" + thread.getId());
            return thread;
        });
        this.scheduledThreadPoolExecutor.scheduleWithFixedDelay(new HeartbeatRunner(), 0, HEARTBEAT_TIME, TimeUnit.SECONDS);
        this.scheduledThreadPoolExecutor.scheduleWithFixedDelay(new OfflineCheckRunner(), 0, HEARTBEAT_TIME, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        this.scheduledThreadPoolExecutor.shutdownNow();
    }

    @Override
    public String name() {
        return "redisRegistry";
    }


    class HeartbeatRunner implements Runnable {

        @Override
        public void run() {
            double now = System.currentTimeMillis();
            if (!localDispatcherList.isEmpty()) {
                LOGGER.debug("heartbeat for dispatchers:{}", localDispatcherList);
                redisTemplate.opsForZSet().add(DISPATCHER_KEY,
                        localDispatcherList.stream().map(dispatcher -> new DefaultTypedTuple<>(dispatcher, now)).collect(Collectors.toSet()));
            }
            if (!localProcessList.isEmpty()) {
                LOGGER.debug("heartbeat for processes:{}", localProcessList);
                redisTemplate.opsForZSet().add(DISPATCHER_KEY,
                        localProcessList.stream().map(process -> new DefaultTypedTuple<>(process, now)).collect(Collectors.toSet()));
            }
        }

    }


    class OfflineCheckRunner implements Runnable {

        @Override
        public void run() {
            long max = System.currentTimeMillis();
            long min = max - HEARTBEAT_TIMEOUT_TIME * 1000;
            Set<String> dispatcherResult = redisTemplate.opsForZSet().rangeByScore(DISPATCHER_KEY, min, max);
            if (dispatcherResult != null && !dispatcherResult.isEmpty()) {
                dispatcherList = new ArrayList<>(dispatcherResult);
            } else {
                dispatcherList = new ArrayList<>();
            }
            Set<String> processResult = redisTemplate.opsForZSet().rangeByScore(DISPATCHER_KEY, min, max);
            if (processResult != null && !processResult.isEmpty()) {
                processList = new ArrayList<>(processResult);
            } else {
                processList = new ArrayList<>();
            }
            LOGGER.debug("online dispatchers:{}", dispatcherList);
            LOGGER.debug("online processes:{}", processList);
        }
    }

}
