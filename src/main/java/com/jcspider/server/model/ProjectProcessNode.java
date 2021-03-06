package com.jcspider.server.model;


/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
public class ProjectProcessNode {

    private long    id;
    private long    projectId;
    private String  processNode;
    private Long   createdAt;

    public ProjectProcessNode() {
    }

    public ProjectProcessNode(long projectId, String processNode, Long createdAt) {
        this.projectId = projectId;
        this.processNode = processNode;
        this.createdAt = createdAt;
    }

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

    public String getProcessNode() {
        return processNode;
    }

    public void setProcessNode(String processNode) {
        this.processNode = processNode;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
