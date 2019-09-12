package com.jcspider.server.component.core;

import com.jcspider.server.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhuang.hu Date:2019-09-10 Time:10:07
 */
public class ProcessRunner implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessRunner.class);

    private JCProcess   process;
    private Task        task;

    public ProcessRunner(JCProcess process, Task task) {
        this.process = process;
        this.task = task;
    }

    public Long getProjectId() {
        return this.task.getProjectId();
    }

    @Override
    public void run() {
        try {
            process.processTask(task.getId());
        } catch (Exception e) {
            LOGGER.error("process error", e);
        }
    }
}
