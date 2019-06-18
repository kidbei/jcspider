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
}
