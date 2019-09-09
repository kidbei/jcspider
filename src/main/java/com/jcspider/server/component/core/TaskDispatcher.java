package com.jcspider.server.component.core;

import com.jcspider.server.component.core.event.NewTask;
import com.jcspider.server.component.core.event.PopTaskReq;
import com.jcspider.server.component.core.event.PopTaskResp;
import com.jcspider.server.component.ifc.JCComponent;
import com.jcspider.server.component.ifc.JCQueue;
import com.jcspider.server.component.ifc.ResultExporter;
import com.jcspider.server.dao.TaskDao;
import com.jcspider.server.model.Task;
import com.jcspider.server.utils.Constant;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhuang.hu Date:2019-09-06 Time:19:12
 */
@Component
public class TaskDispatcher implements JCComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskDispatcher.class);


    private ScheduledThreadPoolExecutor bufferScheduler = new ScheduledThreadPoolExecutor(1);
    private ThreadPoolExecutor          persistenceExecutor = new ThreadPoolExecutor(1, 1, Integer.MAX_VALUE, TimeUnit.HOURS, new LinkedBlockingQueue<>());
    private ThreadPoolExecutor          popTaskExecutor = new ThreadPoolExecutor(1, 2, Integer.MAX_VALUE, TimeUnit.HOURS, new LinkedBlockingQueue<>());

    @Autowired
    private ApplicationContext      applicationContext;
    @Autowired
    private TaskDao                 taskDao;
    @Autowired
    private JCQueue                 jcQueue;

    private List<ResultExporter>    resultExporters;
    private List<Task>              taskBuffer = new ArrayList<>();

    private NewTaskEvent            newTaskEvent = new NewTaskEvent();
    private PopTaskEvent            popTaskEvent = new PopTaskEvent();


    @Override
    public void start(){
        this.resultExporters = new ArrayList<>(this.applicationContext.getBeansOfType(ResultExporter.class).values());
        bufferScheduler.scheduleWithFixedDelay(new FlushTaskBuffer(), 0, 1, TimeUnit.SECONDS);
        this.jcQueue.subscribe(Constant.TOPIC_NEW_TASK, this.newTaskEvent);
        this.jcQueue.subscribe(Constant.TOPIC_POP_TASK_REQ, this.popTaskEvent);
    }

    @Override
    public void shutdown() {

    }

    @Override
    public String name() {
        return "taskDispatcher";
    }


    class FlushTaskBuffer implements Runnable {

        @Override
        public void run() {
            List<Task> bufferList;
            synchronized (taskBuffer) {
                bufferList = new ArrayList<>(taskBuffer);
                taskBuffer = new ArrayList<>();
            }
            persistenceExecutor.execute(() -> new TaskPersistence(bufferList));
        }

    }


    class NewTaskEvent implements OnEvent {
        @Override
        public void event(String topic, Object value) {
            NewTask newTask = (NewTask)value;
            LOGGER.info("new task : {}", newTask.task.getSourceUrl());
            taskBuffer.add(newTask.task);
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
                        LOGGER.info("no new task found for project:{}, stop it", popTaskReq.projectId);
                        jcQueue.publish(Constant.TOPIC_STOP_PROJECT, popTaskReq.projectId);
                    }
                } catch (Exception e) {
                    LOGGER.error("pop task error", e);
                }
            });
        }

    }


    class TaskPersistence implements Runnable {

        private List<Task> tasks;
        public TaskPersistence(List<Task> tasks) {
            this.tasks = tasks;
        }

        @Override
        public void run() {
            List<Task> existsList = taskDao.findByIds(this.tasks.stream().map(t -> t.getId()).collect(Collectors.toList()));
            if (CollectionUtils.isNotEmpty(existsList)) {
                tasks.removeAll(existsList);
            }
            taskDao.insertBatch(tasks);
        }
    }

}
