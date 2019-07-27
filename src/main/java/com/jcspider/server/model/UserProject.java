package com.jcspider.server.model;


/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
public class UserProject {

    private Long        id;
    private String      uid;
    private String      role;
    private Long   createdAt;
    private Long   updatedAt;
    private Long        projectId;

    public UserProject() {
    }

    public UserProject(String uid, String role, Long projectId) {
        this.uid = uid;
        this.role = role;
        this.projectId = projectId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
