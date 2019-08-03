package com.jcspider.server.model;

/**
 * @Author: Gosin
 * @Date: 2019-08-03 22:12
 */
public class LogQueryExp {

    private Long    projectId;
    private String  taskId;
    private String  level;
    private String  logTimeStart;
    private String  logTimeEnd;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLogTimeStart() {
        return logTimeStart;
    }

    public void setLogTimeStart(String logTimeStart) {
        this.logTimeStart = logTimeStart;
    }

    public String getLogTimeEnd() {
        return logTimeEnd;
    }

    public void setLogTimeEnd(String logTimeEnd) {
        this.logTimeEnd = logTimeEnd;
    }
}
