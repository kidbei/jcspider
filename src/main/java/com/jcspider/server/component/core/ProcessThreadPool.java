package com.jcspider.server.component.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhuang.hu Date:2019-09-10 Time:11:18
 */
public class ProcessThreadPool extends ThreadPoolExecutor {

    private final ConcurrentHashMap<Long, AtomicInteger> pendingCountMap = new ConcurrentHashMap<>();


    public ProcessThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }


    @Override
    public void execute(Runnable command) {
        if (command instanceof ProcessRunner) {
            ProcessRunner processRunner = (ProcessRunner) command;
            this.incrPending(processRunner.getProjectId(), 1);
            super.execute(() -> {
                try {
                    command.run();
                } finally {
                    this.incrPending(processRunner.getProjectId(), -1);
                }
            });
        } else {
            super.execute(command);
        }

    }


    private void incrPending(Long projectId, int incrValue) {
        AtomicInteger ai = pendingCountMap.get(projectId);
        if (ai == null) {
            ai = new AtomicInteger(0);
            pendingCountMap.put(projectId, ai);
        }
        if (incrValue > 0) {
            for (int i = 0; i < incrValue; i++) {
                ai.incrementAndGet();
            }
        } else if (incrValue < 0){
            for (int i = 0; i < Math.abs(incrValue); i++) {
                ai.decrementAndGet();
            }
        }
    }


    public int  getPendingCount(Long projectId) {
        AtomicInteger ai = this.pendingCountMap.get(projectId);
        return ai == null ? 0 : ai.get();
    }

    public void resetPending(Long projectId) {
        this.pendingCountMap.remove(projectId);
    }

}
