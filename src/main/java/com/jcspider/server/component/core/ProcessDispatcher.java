package com.jcspider.server.component.core;

import com.jcspider.server.component.core.event.PopTaskResp;
import com.jcspider.server.component.ifc.JCComponent;
import com.jcspider.server.component.ifc.JCQueue;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.model.DebugResult;
import com.jcspider.server.model.DebugTask;
import com.jcspider.server.model.Project;
import com.jcspider.server.utils.Constant;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhuang.hu Date:2019-09-09 Time:17:17
 */
@Component
public class ProcessDispatcher implements JCComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDispatcher.class);

    @Autowired
    private JCQueue     jcQueue;
    @Autowired
    private ProjectDao  projectDao;
    @Autowired
    private JSR223EngineProcess process;

    @Value("${process.threads:10}")
    private int processThreads;

    private ThreadPoolExecutor  popThreadPool;
    private ThreadPoolExecutor  processThreadPool;

    private final ConcurrentHashMap<Long, TaskPopRunner>  projectTaskPopRunnerMap = new ConcurrentHashMap<>();


    private final StartProjectEvent   startProjectEvent = new StartProjectEvent();
    private final PopTaskRespEvent    popTaskRespEvent = new PopTaskRespEvent();
    private final StopProjectEvent    stopProjectEvent = new StopProjectEvent();
    private final DebugProjectReqEvent    debugProjectReqEvent = new DebugProjectReqEvent();



    @Override
    public void start(){
        this.popThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                1L, TimeUnit.HOURS,
                new LinkedBlockingQueue<>());
        this.processThreadPool = new ThreadPoolExecutor(processThreads, processThreads,
                Integer.MAX_VALUE, TimeUnit.HOURS,
                new LinkedBlockingQueue<>());
        jcQueue.subscribe(Constant.TOPIC_START_PROJECT, startProjectEvent);
        jcQueue.subscribe(Constant.TOPIC_POP_TASK_RESP, popTaskRespEvent);
        jcQueue.subscribe(Constant.TOPIC_STOP_PROJECT, stopProjectEvent);
        jcQueue.subscribe(Constant.TOPIC_DEBUG_PROJECT_REQ, debugProjectReqEvent);
    }


    class StartProjectEvent implements OnEvent {

        @Override
        public void event(String topic, Object value) {
            final long projectId = (long) value;
            startProject(projectId);
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void startProject(long projectId) {
        Project project = projectDao.getById(projectId);
        LOGGER.info("start process for project:{}", projectId);
        projectDao.updateStatusById(projectId, Constant.PROJECT_STATUS_START);
        TaskPopRunner taskPopRunner = new TaskPopRunner(jcQueue, projectId, project.getQps());
        popThreadPool.execute(taskPopRunner);
        projectTaskPopRunnerMap.put(projectId, taskPopRunner);
        processThreadPool.execute(() -> process.startProject(projectId));
        if (project.getScheduleType().equals(Constant.SCHEDULE_TYPE_LOOP)) {
            LOGGER.info("register repeat scheduler for project:{}", projectId);
            try {
                RepeatJobFactory.registerProjectRepeatJob(projectId, project.getScheduleValue());
            } catch (SchedulerException e) {
                LOGGER.error("register repeat scheduler error", e);
            }
        }

    }


    class PopTaskRespEvent implements OnEvent {

        @Override
        public void event(String topic, Object value) {
            final PopTaskResp popTaskResp = (PopTaskResp)value;
            processThreadPool.execute(() -> popTaskResp.tasks.forEach(task -> process.processTask(task.getId())));
        }
    }


    class StopProjectEvent implements OnEvent {

        @Override
        public void event(String topic, Object value) {
            long projectId = (long) value;
            LOGGER.info("stop project process, projectId:{}", projectId);
            TaskPopRunner taskPopRunner = projectTaskPopRunnerMap.remove(projectId);
            if (taskPopRunner != null) {
                taskPopRunner.stop();
            }
            projectDao.updateStatusById(projectId, Constant.PROJECT_STATUS_STOP);
            try {
                RepeatJobFactory.stopRegisterProjectRepeatJob(projectId);
            } catch (SchedulerException e) {
                LOGGER.error("unregister repeat scheduler error", e);
            }
        }

    }


    class DebugProjectReqEvent implements OnEvent {

        @Override
        public void event(String topic, Object value) {
            final DebugTask debugTask = (DebugTask) value;
            processThreadPool.execute(() -> {
                try {
                    DebugResult debugResult = process.debug(debugTask.getRequestId(), debugTask.getScriptText(), debugTask.getSimpleTask());
                    jcQueue.publish(Constant.TOPIC_DEBUG_PROJECT_RESP, debugResult);
                    LOGGER.info("debug success:{}", debugTask);
                } catch (Exception e) {
                    LOGGER.error("debug error", e);
                }
            });
        }
    }


    @Override
    public void shutdown() {
        this.popThreadPool.shutdown();
        this.processThreadPool.shutdown();
    }

    @Override
    public String name() {
        return "processDispatcher";
    }
}
