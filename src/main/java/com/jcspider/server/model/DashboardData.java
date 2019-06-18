package com.jcspider.server.model;

/**
 * @Author: Gosin
 * @Date: 2019-06-14 23:22
 */
public class DashboardData {

    private int    projectCount;
    private int    projectRunningCount;
    private int    taskCount;
    private int    resultCount;

    public int getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(int projectCount) {
        this.projectCount = projectCount;
    }

    public int getProjectRunningCount() {
        return projectRunningCount;
    }

    public void setProjectRunningCount(int projectRunningCount) {
        this.projectRunningCount = projectRunningCount;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
}
