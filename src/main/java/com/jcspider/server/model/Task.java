package com.jcspider.server.model;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class Task extends SimpleTask {

    private static final long serialVersionUID = -6807799756209612420L;
    private String status;
    private String  scheduleType;
    private String  stack;
    private Long    projectId;
    private Long    scheduleValue;
    @JsonSerialize(using = LongTimeFormat.Serialize.class)
    private Long   createdAt;
    @JsonSerialize(using = LongTimeFormat.Serialize.class)
    private Long   updatedAt;
    private String  processNode;

    public Task() {
    }

    public Task(String id, String status) {
        this.setId(id);
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getScheduleValue() {
        return scheduleValue;
    }

    public void setScheduleValue(Long scheduleValue) {
        this.scheduleValue = scheduleValue;
    }

    @Override
    public boolean equals(Object obj) {
        return this.getId().equals(((Task)obj).getId());
    }

    public String getProcessNode() {
        return processNode;
    }

    public void setProcessNode(String processNode) {
        this.processNode = processNode;
    }
}
