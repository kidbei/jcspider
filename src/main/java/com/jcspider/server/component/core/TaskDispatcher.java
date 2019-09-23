package com.jcspider.server.component.core;

import com.jcspider.server.component.core.event.NewTask;
import com.jcspider.server.component.core.event.PopTaskReq;
import com.jcspider.server.component.core.event.PopTaskResp;
import com.jcspider.server.component.ifc.JCComponent;
import com.jcspider.server.component.ifc.JCQueue;
import com.jcspider.server.component.ifc.ResultExporter;
import com.jcspider.server.dao.ProjectDao;
import com.jcspider.server.dao.TaskDao;
import com.jcspider.server.model.Project;
import com.jcspider.server.model.Task;
import com.jcspider.server.model.TaskResult;
import com.jcspider.server.utils.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhuang.hu Date:2019-09-06 Time:19:12
 */
@Component
public class TaskDispatcher implements JCComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskDispatcher.class);

    private static final int    FLUSH_DELAY = 1;

    private ThreadPoolExecutor          popTaskExecutor = new ThreadPoolExecutor(1, 2, Integer.MAX_VALUE, TimeUnit.HOURS, new LinkedBlockingQueue<>());
    private ThreadPoolExecutor          persistenceExecutor = new ThreadPoolExecutor(1, 2, Integer.MAX_VALUE, TimeUnit.HOURS, new LinkedBlockingQueue<>());

    @Autowired
    private TaskDao                 taskDao;
    @Autowired
    private ProjectDao              projectDao;
    @Autowired
    private JCQueue                 jcQueue;

    @Value("${process.result.exporter}")
    private String                  exportComponents;
    @Autowired
    private ApplicationContext      applicationContext;

    private TaskBuffer              taskBuffer = new TaskBuffer();

    protected List<ResultExporter>  resultExporters = new ArrayList<>();


    private NewTaskEvent            newTaskEvent = new NewTaskEvent();
    private PopTaskEvent            popTaskEvent = new PopTaskEvent();
    private ResultExportEvent       resultExportEvent = new ResultExportEvent();


    @Override
    public void start(){
        this.jcQueue.subscribe(Constant.TOPIC_NEW_TASK, this.newTaskEvent);
        this.jcQueue.subscribe(Constant.TOPIC_POP_TASK_REQ, this.popTaskEvent);
        this.jcQueue.subscribe(Constant.TOPC_EXPORT_RESULT, this.resultExportEvent);
        List<String> exportComponentList = Arrays.asList(this.exportComponents.split(","));
        if (exportComponentList.contains(Constant.DB_RESULT_EXPORTER)) {
            this.resultExporters.add(applicationContext.getBean(Constant.DB_RESULT_EXPORTER, DbResultExporter.class));
        }
        this.persistenceExecutor.execute(new PersistenceRunner());
        recoveryProject();
    }

    @Override
    public void shutdown() {
        this.persistenceExecutor.shutdown();
        this.popTaskExecutor.shutdown();
    }

    @Override
    public String name() {
        return "taskDispatcher";
    }


    private void recoveryProject() {
        List<Project> projects = projectDao.findAll();
        if (CollectionUtils.isNotEmpty(projects)) {
            projects.forEach(project -> {
                if (project.getStatus().equals(Constant.PROJECT_STATUS_START)) {
                    jcQueue.publish(Constant.TOPIC_RECOVERY_PROJECT, project.getId());
                } else {
                    if (project.getScheduleType().equals(Constant.SCHEDULE_TYPE_LOOP)) {
                        jcQueue.publish(Constant.TOPIC_RECOVERY_LOOP, project.getId());
                    }
                }
            });
        }
    }


    class NewTaskEvent implements OnEvent {
        @Override
        public void event(String topic, Object value) {
            NewTask newTask = (NewTask)value;
            newTask.task.setStatus(Constant.TASK_STATUS_NONE);
            LOGGER.info("new task : {}", newTask.task.getSourceUrl());
            taskBuffer.add(newTask.task);
        }
    }

    class ResultExportEvent implements OnEvent {

        @Override
        public void event(String topic, Object value) {
            TaskResult taskResult = (TaskResult) value;
            LOGGER.info("save task result:{}", taskResult.getTaskId());
            resultExporters.forEach(resultExporter -> {
                try {
                    resultExporter.export(taskResult);
                } catch (Exception e) {
                    LOGGER.error("export result error, exporter:{}", resultExporter, e);
                }
            });
        }
    }


    class PopTaskEvent implements OnEvent {

        @Override
        public void event(String topic, Object value) {
            final PopTaskReq popTaskReq = (PopTaskReq)value;
            LOGGER.info("pop task for project:{}", popTaskReq.projectId);
            popTaskExecutor.execute(() -> {
                try {
                    List<Task> tasks = taskDao.findByProjectIdAndStatus(popTaskReq.projectId, Constant.TASK_STATUS_NONE, popTaskReq.popSize);
                    if (CollectionUtils.isNotEmpty(tasks)) {
                        taskDao.updateStatusByIds(tasks.stream().map(t -> t.getId()).collect(Collectors.toList()), Constant.TASK_STATUS_RUNNING);
                        jcQueue.publish(Constant.TOPIC_POP_TASK_RESP, new PopTaskResp(popTaskReq.projectId, tasks));
                    } else {
                        if (!taskBuffer.containsProject(popTaskReq.projectId)) {
                            LOGGER.info("no new task found for project:{}", popTaskReq.projectId);
                            jcQueue.publish(Constant.TOPIC_NO_MORE_TASK, popTaskReq.projectId);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("pop task error", e);
                }
            });
        }

    }

    class PersistenceRunner implements Runnable {
        @Override
        public void run() {
            while (!Thread.interrupted()) {
                try {
                    if (!taskBuffer.isEmpty()) {
                        synchronized (taskBuffer) {
                            long now = System.currentTimeMillis();
                            List<Task> existsTaskList = taskDao.findByIds(taskBuffer.stream().map(t -> t.getId()).collect(Collectors.toList()));
                            List<String>  toDeleteTaskList = new ArrayList<>();
                            if (CollectionUtils.isNotEmpty(existsTaskList)) {
                                for (Task task : existsTaskList) {
                                    if (task.getExpireValue() != null && task.getExpireValue() > 0) {
                                        if (now - task.getCreatedAt() >= task.getExpireValue()) {
                                            LOGGER.info("task {} is expired", task.getId());
                                            toDeleteTaskList.add(task.getId());
                                        } else {
                                            taskBuffer.remove(task);
                                        }
                                    }
                                }
                                if (CollectionUtils.isNotEmpty(toDeleteTaskList)) {
                                    taskDao.deleteByIds(toDeleteTaskList);
                                    taskDao.deleteByFromTaskIds(toDeleteTaskList);
                                }
                            }
                            if (!taskBuffer.isEmpty()) {
                                taskDao.insertBatch(taskBuffer);
                                taskBuffer.clear();
                            }
                        }
                    }
                    Thread.sleep(FLUSH_DELAY * 1000);
                } catch (Exception e) {
                    LOGGER.error("persistence task buffer error", e);
                }
            }
        }
    }

}
