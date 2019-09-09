package com.jcspider.server.component.core.event;

import com.jcspider.server.model.Task;

/**
 * @author zhuang.hu Date:2019-09-06 Time:19:32
 */
public class NewTask {

    public Task task;

    public NewTask(Task task) {
        this.task = task;
    }
}
