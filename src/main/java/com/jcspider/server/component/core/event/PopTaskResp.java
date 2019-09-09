package com.jcspider.server.component.core.event;

import com.jcspider.server.model.Task;

import java.util.List;

/**
 * @author zhuang.hu Date:2019-09-09 Time:16:38
 */
public class PopTaskResp {

    public long         projectId;
    public List<Task>   tasks;

    public PopTaskResp(long projectId, List<Task> tasks) {
        this.projectId = projectId;
        this.tasks = tasks;
    }
}
