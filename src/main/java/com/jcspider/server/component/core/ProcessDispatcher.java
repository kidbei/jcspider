package com.jcspider.server.component.core;

import com.jcspider.server.component.core.event.PopTaskResp;
import com.jcspider.server.component.ifc.JCComponent;
import com.jcspider.server.component.ifc.JCQueue;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.model.DebugResult;
import com.jcspider.server.model.DebugTask;
import com.jcspider.server.model.Project;
import com.jcspider.server.utils.Constant;
import com.jcspider.server.web.api.service.SelfLogService;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.*;

/**
 * @author zhuang.hu Date:2019-09-09 Time:17:17
 */
@Component
public class ProcessDispatcher implements JCComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessDispatcher.class);

    private static final int    MAX_NO_MORE_TIME = 5;

    @Autowired
    private JCQueue     jcQueue;
    @Autowired
    private ProjectDao  projectDao;
    @Autowired
    private JSR223EngineProcess process;
    @Autowired
    private SelfLogService  selfLogService;

    @Value("${process.threads:10}")
    private int processThreads;


    private ExecutorService  popThreadPool;
    private ProcessThreadPool  processThreadPool;

    private final ConcurrentHashMap<Long, TaskPopRunner>  projectTaskPopRunnerMap = new ConcurrentHashMap<>();


    private final StartProjectEvent     startProjectEvent = new StartProjectEvent();
    private final PopTaskRespEvent      popTaskRespEvent = new PopTaskRespEvent();
    private final StopProjectEvent      stopProjectEvent = new StopProjectEvent();
    private final DebugProjectReqEvent  debugProjectReqEvent = new DebugProjectReqEvent();
    private final NoMoreTaskEvent       noMoreTaskEvent = new NoMoreTaskEvent();
    private final RecoveryProjectEvent  recoveryProjectEvent = new RecoveryProjectEvent();



    @Override
    public void start(){
        this.popThreadPool = Executors.newCachedThreadPool();
        this.processThreadPool = new ProcessThreadPool(processThreads, processThreads,
                Integer.MAX_VALUE, TimeUnit.HOURS,
                new LinkedBlockingQueue<>());
                jcQueue.subscribe(Constant.TOPIC_START_PROJECT, startProjectEvent);
        jcQueue.subscribe(Constant.TOPIC_POP_TASK_RESP, popTaskRespEvent);
        jcQueue.subscribe(Constant.TOPIC_STOP_PROJECT, stopProjectEvent);
        jcQueue.subscribe(Constant.TOPIC_DEBUG_PROJECT_REQ, debugProjectReqEvent);
        jcQueue.subscribe(Constant.TOPIC_NO_MORE_TASK, noMoreTaskEvent);
        jcQueue.subscribe(Constant.TOPIC_RECOVERY_PROJECT, recoveryProjectEvent);
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
        if (project.getStatus().equals(Constant.PROJECT_STATUS_START) && projectTaskPopRunnerMap.contains(projectId)) {
            LOGGER.info("project is already start, projectId:{}", projectId);
            return;
        }
        projectDao.updateStatusById(projectId, Constant.PROJECT_STATUS_START);
        TaskPopRunner taskPopRunner = new TaskPopRunner(this.jcQueue, this.processThreadPool, projectId, project.getQps());
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
        selfLogService.addLog(projectId, Constant.LEVEL_INFO, "启动项目:" + project.getId());
    }


    class PopTaskRespEvent implements OnEvent {

        @Override
        public void event(String topic, Object value) {
            final PopTaskResp popTaskResp = (PopTaskResp)value;
            popTaskResp.tasks.forEach(task -> processThreadPool.execute(new ProcessRunner(process,task)));
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
            processThreadPool.resetPending(projectId);
            projectDao.updateStatusById(projectId, Constant.PROJECT_STATUS_STOP);
            try {
                RepeatJobFactory.stopRegisterProjectRepeatJob(projectId);
            } catch (SchedulerException e) {
                LOGGER.error("unregister repeat scheduler error", e);
            }
            selfLogService.addLog(projectId, Constant.LEVEL_INFO, "停止项目:" + projectId);
        }

    }


    class RecoveryProjectEvent implements OnEvent {

        @Override
        public void event(String topic, Object value) {
            long projectId = (long) value;
            LOGGER.info("recovery project:{}", projectId);
            startProject(projectId);
        }
    }


    class NoMoreTaskEvent implements OnEvent {

        @Override
        public void event(String topic, Object value) {
            long projectId = (long) value;
            if (processThreadPool.getPendingCount(projectId) <= 0) {
                LOGGER.info("no more task and pending count is 0, stop project:{}", projectId);
                TaskPopRunner taskPopRunner = projectTaskPopRunnerMap.get(projectId);
                if (taskPopRunner == null) {
                    jcQueue.publish(Constant.TOPIC_STOP_PROJECT, projectId);
                    LOGGER.info("no task pop runner, stop project:{}", projectId);
                } else {
                    if (taskPopRunner.incrNoMoreTime() >= MAX_NO_MORE_TIME) {
                        jcQueue.publish(Constant.TOPIC_STOP_PROJECT, projectId);
                        LOGGER.info("max no more time is {}, stop project:{}", MAX_NO_MORE_TIME, projectId);
                    }
                }
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
