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
    private double          oldQps = 0.;
    private RateLimiter     rateLimiter = RateLimiter.create(1);
    private int             batchSize;
    private volatile        boolean stop = false;

    private JCQueue         jcQueue;
    private ProcessThreadPool    processThreadPool;

    private int             noMoreTime;


    public TaskPopRunner(JCQueue jcQueue, ProcessThreadPool processThreadPool, long projectId, double qps) {
        this.jcQueue = jcQueue;
        this.processThreadPool = processThreadPool;
        this.projectId = projectId;
        this.initQpsRate(qps);
    }


    private void initQpsRate(double qps) {
        this.qps = qps;
        if (qps < 1) {
            this.batchSize = 1;
        } else {
            this.batchSize = (int) Math.ceil(this.qps);
        }
        this.rateLimiter.setRate(this.qps);
    }


    public void stop() {
        this.stop = true;
    }


    public void setNoMoreTime(int noMoreTime) {
        this.noMoreTime = noMoreTime;
    }

    public int incrNoMoreTime() {
        this.noMoreTime += 1;
        return this.noMoreTime;
    }


    @Override
    public void run() {
        while (!this.stop) {
            this.rateLimiter.acquire(this.batchSize);
            LOGGER.info("pop {} task for project:{}", this.batchSize, this.projectId);
            jcQueue.publish("pop_task_req", new PopTaskReq(this.projectId, this.batchSize));
            if (this.processThreadPool.getPendingCount(this.projectId) > (this.batchSize * 2)) {
                if (this.qps > 1) {
                    this.oldQps = this.qps;
                    this.initQpsRate(this.qps - 1);
                }
                LOGGER.info("pending is overflow, projectId:{}, current qps:{}, current batch size:{}", this.projectId, this.qps, this.batchSize);
            } else {
                if (this.oldQps != 0.) {
                    this.initQpsRate(this.oldQps);
                    this.oldQps = 0.;
                    LOGGER.info("recovery qps, projectId:{}, current qps:{}, current batch size:{}", this.projectId, this.qps, this.batchSize);
                }
            }
        }
        LOGGER.info("stop project pop task,projectId:{}", this.projectId);
    }


}
