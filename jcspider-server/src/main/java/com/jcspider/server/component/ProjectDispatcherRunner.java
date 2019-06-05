package com.jcspider.server.component;

import com.jcspider.server.dao.TaskDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
@Component
public class ProjectDispatcherRunner implements Runnable {

    private static TaskDao  taskDao;
    private static JCQueue  jcQueue;

    private int rateNumber;
    private List<String> processNodes;
    private volatile boolean stop;

    public ProjectDispatcherRunner(int rateNumber, List<String> processNodes) {
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

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void run() {

    }

}
