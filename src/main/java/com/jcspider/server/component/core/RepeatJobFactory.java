package com.jcspider.server.component.core;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author zhuang.hu Date:2019-09-06 Time:17:15
 */
@Component
public class RepeatJobFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(RepeatJobFactory.class);


    private static StdSchedulerFactory schedulerFactory;


    static {
        schedulerFactory = new StdSchedulerFactory();
    }

    @PostConstruct
    public void shutdown() {
        try {
            schedulerFactory.getScheduler().shutdown(false);
        } catch (SchedulerException e) {
            LOGGER.error("shutdown scheduler error", e);
        }
    }


    public static void registerProjectRepeatJob(long projectId, long runAfter) throws SchedulerException{
        LOGGER.info("register project repeat job for project {}", projectId);
        JobDetail repeatJob = JobBuilder.newJob(RepeatJob.class)
                .usingJobData("projectId", projectId)
                .withIdentity(JobKey.jobKey("repeat_project_" + projectId))
                .build();

        SimpleTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("repeat_project_trigger")
                .withSchedule(
                        SimpleScheduleBuilder.simpleSchedule()
                        .repeatForever()
                        .withIntervalInMilliseconds(runAfter)
                )
                .startNow()
                .build();

        schedulerFactory.getScheduler().scheduleJob(repeatJob, trigger);
    }


    public static void stopRegisterProjectRepeatJob(long projectId) throws SchedulerException {
        LOGGER.info("stop project repeat job for project {}", projectId);
        schedulerFactory.getScheduler().deleteJob(JobKey.jobKey("repeat_project_" + projectId));
    }

}
