package com.jcspider.server.component.core;

import com.jcspider.server.model.Task;

import java.util.*;

/**
 * @Author: Gosin
 * @Date: 2019-09-13 08:55
 */
public class TaskBuffer extends ArrayList<Task> {

    private final Set<Long> projectIdSet = new HashSet<>();

    @Override
    public synchronized boolean add(Task task) {
        projectIdSet.add(task.getProjectId());
        return super.add(task);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends Task> c) {
        c.forEach(t -> projectIdSet.add(t.getProjectId()));
        return super.addAll(c);
    }

    @Override
    public synchronized void clear() {
        this.projectIdSet.clear();
        super.clear();
    }

    public synchronized boolean containsProject(Long projectId) {
        return projectIdSet.contains(projectId);
    }

}
