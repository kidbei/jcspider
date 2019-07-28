package com.jcspider.server.component;

import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.TaskDao;
import com.jcspider.server.model.Project;
import com.jcspider.server.model.Task;
import com.jcspider.server.utils.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
@Component
public class ProjectDispatcherRunner implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDispatcherRunner.class);

    private static TaskDao      taskDao;
    private static ProjectDao   projectDao;
    private static JCQueue      jcQueue;

    private long                projectId;
    private int                 rateNumber;
    private List<String>        processNodes;
    private volatile boolean    stop;
    private int                 idx;
    private long                firstNoTaskTime = 0L;

    public ProjectDispatcherRunner() {
    }

    public ProjectDispatcherRunner(long projectId, int rateNumber, List<String> processNodes) {
        this.projectId = projectId;
        this.rateNumber = rateNumber;
        this.processNodes = processNodes;
    }

    @Autowired
    public void setTaskDao(TaskDao taskDao) {
        ProjectDispatcherRunner.taskDao = taskDao;
    }

    @Autowired
    public void setJcQueue(JCQueue jcQueue) {
        ProjectDispatcherRunner.jcQueue = jcQueue;
    }

    @Autowired
    public void setProjectDao(ProjectDao projectDao) {
        ProjectDispatcherRunner.projectDao = projectDao;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void run() {
        if (this.stop) {
            LOGGER.info("stop dispatcher runner for project {}", this.projectId);
            throw new RuntimeException("stop task");
        }
        try {
            work();
        } catch (Exception e) {
            LOGGER.error("dispatcher error,projectId:{}", projectId);
        }
    }



    private void work() {
        List<Task> tasks = taskDao.findByProjectIdAndStatus(this.projectId, Constant.TASK_STATUS_NONE, this.rateNumber);
        if (CollectionUtils.isEmpty(tasks)) {
            tasks = taskDao.findByOutOfNextRunTime(this.projectId, System.currentTimeMillis(), this.rateNumber);
        }
        if (CollectionUtils.isEmpty(tasks)) {
            LOGGER.info("project {} has no task to crawl", this.projectId);
            if (this.firstNoTaskTime == 0L) {
                this.firstNoTaskTime = System.currentTimeMillis();
            }
            Project project = projectDao.getById(this.projectId);
            if (project.getStatus().equals(Constant.PROJECT_STATUS_START)) {
                if (System.currentTimeMillis() - this.firstNoTaskTime > 1000 * 60L) {
                    LOGGER.info("project {} has no new task, stop it", this.projectId);
                    projectDao.updateStatusById(this.projectId, Constant.PROJECT_STATUS_STOP);
                    DispatcherScheduleFactory.stopProjectRunner(this.projectId);
                    jcQueue.pubDispatcherStop(this.projectId);
                } else {
                    LOGGER.info("project {} has no task now, wait for next schedule time", this.projectId);
                }
            } else {
                LOGGER.info("project {} has no task now, wait for next schedule time", this.projectId);
            }
        } else {
            if (this.firstNoTaskTime != 0L) {
                this.firstNoTaskTime = 0L;
            }
            List<String> taskIds = tasks.stream().map(t -> t.getId()).collect(Collectors.toList());
            taskIds.forEach(taskId -> jcQueue.blockingPushProcessTask(roundProcessNode(), taskId));
            taskDao.updateStatusByIds(taskIds, Constant.TASK_STATUS_RUNNING);
            LOGGER.info("start crawl task list:{}", taskIds);
        }
    }


    private String roundProcessNode() {
        if (this.processNodes.size() == 1) {
            return this.processNodes.get(0);
        }
        if (this.idx < this.processNodes.size()) {
            this.idx ++;
            return this.processNodes.get(this.idx);
        } else {
            this.idx = 0;
            return this.processNodes.get(this.idx);
        }
    }

}
