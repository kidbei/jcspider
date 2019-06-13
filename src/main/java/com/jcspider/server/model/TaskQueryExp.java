package com.jcspider.server.model;

/**
 * @Author: Gosin
 * @Date: 2019-06-13 22:38
 */
public class TaskQueryExp {

    private Long    projectId;
    private String  status;

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
