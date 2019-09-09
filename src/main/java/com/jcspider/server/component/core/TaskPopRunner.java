package com.jcspider.server.component.core;

import com.google.common.util.concurrent.RateLimiter;
import com.jcspider.server.component.core.event.PopTaskReq;
import com.jcspider.server.component.ifc.JCQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuang.hu Date:2019-09-09 Time:17:25
 */
public class TaskPopRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskPopRunner.class);

    private long            projectId;
    private double          qps;
    private RateLimiter     rateLimiter;
    private int             batchSize;
    private volatile    boolean stop;

    private JCQueue         jcQueue;

    public TaskPopRunner(JCQueue jcQueue, long projectId, double qps) {
        this.jcQueue = jcQueue;
        this.projectId = projectId;
        this.qps = qps;
        if (qps < 1) {
            this.batchSize = 1;
        } else {
            this.batchSize = (int) Math.ceil(this.qps);
        }
        this.rateLimiter = RateLimiter.create(qps);
    }


    public void stop() {
        this.stop = true;
    }


    @Override
    public void run() {
        while (!this.stop) {
            this.rateLimiter.acquire(this.batchSize);
            LOGGER.info("pop {} task for project:{}", this.batchSize, this.projectId);
            jcQueue.publish("pop_task_req", new PopTaskReq(this.projectId, this.batchSize));
        }
        LOGGER.info("stop project pop task,projectId:{}", this.projectId);
    }


}
