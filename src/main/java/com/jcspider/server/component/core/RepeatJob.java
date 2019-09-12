package com.jcspider.server.component.core;

import com.jcspider.server.component.ifc.JCQueue;
import com.jcspider.server.utils.Constant;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhuang.hu Date:2019-09-06 Time:17:12
 */
@Component
public class RepeatJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepeatJob.class);


    private static JCQueue jcQueue;

    @Autowired
    public void setJcQueue(JCQueue jcQueue) {
        RepeatJob.jcQueue = jcQueue;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Long projectId = jobExecutionContext.getJobDetail().getJobDataMap().getLong("projectId");
        LOGGER.info("start to running project again, projectId:{}", projectId);
        jcQueue.publish(Constant.TOPIC_START_PROJECT, projectId);
    }


}
