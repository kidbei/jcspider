package com.jcspider.server.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class Task extends SimpleTask {

    private String  status;
    private String  scheduleType;
    private String  stack;
    private long    projectId;
    private long    scheduleValue;
    private Timestamp   createdAt;
    private Timestamp   updatedAt;


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

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getScheduleValue() {
        return scheduleValue;
    }

    public void setScheduleValue(long scheduleValue) {
        this.scheduleValue = scheduleValue;
    }

    @Override
    public boolean equals(Object obj) {
        return this.getId().equals(((Task)obj).getId());
    }
}
