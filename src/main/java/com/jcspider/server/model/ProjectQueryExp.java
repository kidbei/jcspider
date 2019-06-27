package com.jcspider.server.model;

/**
 * @Date: 2019-06-13 23:20
 * @author zhuang.hu
 * @since 14 June 2019
 */
public class ProjectQueryExp {

    private String  uid;
    private String  status;
    private String  name;
    private Long    projectId;
    private String  description;
    private String  createdAtStart;
    private String  createdAtEnd;
    private String  updatedAtStart;
    private String  updatedAtEnd;


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getCreatedAtStart() {
        return createdAtStart;
    }

    public void setCreatedAtStart(String createdAtStart) {
        this.createdAtStart = createdAtStart;
    }

    public String getCreatedAtEnd() {
        return createdAtEnd;
    }

    public void setCreatedAtEnd(String createdAtEnd) {
        this.createdAtEnd = createdAtEnd;
    }

    public String getUpdatedAtStart() {
        return updatedAtStart;
    }

    public void setUpdatedAtStart(String updatedAtStart) {
        this.updatedAtStart = updatedAtStart;
    }

    public String getUpdatedAtEnd() {
        return updatedAtEnd;
    }

    public void setUpdatedAtEnd(String updatedAtEnd) {
        this.updatedAtEnd = updatedAtEnd;
    }
}
