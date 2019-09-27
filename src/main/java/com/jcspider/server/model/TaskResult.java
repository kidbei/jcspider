package com.jcspider.server.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class TaskResult implements Serializable {

    private static final long serialVersionUID = -1575371272023064284L;
    private Long        id;
    private Long        projectId;
    private String      taskId;
    private String      resultText;
    @JsonSerialize(using = LongTimeFormat.Serialize.class)
    private Long        createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TaskResult{");
        sb.append("id=").append(id);
        sb.append(", projectId=").append(projectId);
        sb.append(", taskId='").append(taskId).append('\'');
        sb.append(", resultText='").append(resultText.length() > 1024 ? resultText.substring(0, 1023) : resultText).append('\'');
        sb.append(", createdAt=").append(createdAt);
        sb.append('}');
        return sb.toString();
    }
}
