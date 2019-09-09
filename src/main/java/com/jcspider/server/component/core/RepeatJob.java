package com.jcspider.server.component.core;

import com.jcspider.server.dao.ProjectDao;
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


    private static ProjectDao   projectDao;

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        RepeatJob.projectDao = projectDao;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Long projectId = jobExecutionContext.getJobDetail().getJobDataMap().getLongFromString("projectId");
        LOGGER.info("start to running project again, projectId:{}", projectId);
    }


}
