package com.jcspider.server.component.core;

import com.google.common.collect.ArrayListMultimap;
import com.jcspider.server.component.core.event.NewTask;
import com.jcspider.server.component.core.event.PopTaskReq;
import com.jcspider.server.component.core.event.PopTaskResp;
import com.jcspider.server.component.ifc.JCComponent;
import com.jcspider.server.component.ifc.JCQueue;
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
    private ThreadPoolExecutor          persistenceExecutor = new ThreadPoolExecutor(1, 1, Integer.MAX_VALUE, TimeUnit.HOURS, new LinkedBlockingQueue<>());

    @Autowired
    private TaskDao                 taskDao;
    @Autowired
    private ProjectDao              projectDao;

    @Autowired
    private JCQueue                 jcQueue;

    private ArrayListMultimap<Long, Task>   taskBuffer = ArrayListMultimap.create();

    private NewTaskEvent            newTaskEvent = new NewTaskEvent();
    private PopTaskEvent            popTaskEvent = new PopTaskEvent();


    @Override
    public void start(){
        this.jcQueue.subscribe(Constant.TOPIC_NEW_TASK, this.newTaskEvent);
        this.jcQueue.subscribe(Constant.TOPIC_POP_TASK_REQ, this.popTaskEvent);
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
            taskBuffer.put(newTask.task.getProjectId(), newTask.task);
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
                        LOGGER.info("no new task found for project:{}", popTaskReq.projectId);
                        jcQueue.publish(Constant.TOPIC_NO_MORE_TASK, popTaskReq.projectId);
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
                            for (Long projectId: taskBuffer.keySet()) {
                                List<Task> taskList = taskBuffer.get(projectId);
                                if (!taskList.isEmpty()) {
                                    List<Task> existsTaskList = taskDao.findByIds(taskList.stream().map(t -> t.getId()).collect(Collectors.toList()));
                                    if (!existsTaskList.isEmpty()) {
                                        taskList.removeAll(existsTaskList);
                                    }
                                    if (!taskList.isEmpty()) {
                                        taskDao.insertBatch(taskList);
                                    } else {
                                        LOGGER.info("no new task to persistence, projectId:{}", projectId);
                                    }
                                }
                            }
                            taskBuffer.clear();
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("persistence task buffer error", e);
                }

                try {
                    Thread.sleep(FLUSH_DELAY * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
