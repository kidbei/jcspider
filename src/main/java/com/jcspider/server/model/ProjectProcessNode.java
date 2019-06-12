package com.jcspider.server.model;

import java.sql.Timestamp;

/**
 * @author zhuang.hu
 * @since 05 June 2019
 */
public class ProjectProcessNode {

    private long    id;
    private long    projectId;
    private String  processNode;
    private Timestamp   createdAt;

    public ProjectProcessNode() {
    }

    public ProjectProcessNode(long projectId, String processNode, Timestamp createdAt) {
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
