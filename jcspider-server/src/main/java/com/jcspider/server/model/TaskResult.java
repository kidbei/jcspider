package com.jcspider.server.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class TaskResult implements Serializable {

    private long        id;
    private long        projectId;
    private String      taskId;
    private String      resultText;
    private Timestamp   createdAt;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
