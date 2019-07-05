package com.jcspider.server.model;

/**
 * @author zhuang.hu Date:2019-07-03 Time:20:30
 */
public class ProjectResultCount {

    private long    projectId;
    private int     resultCount;

    public ProjectResultCount() {
    }

    public ProjectResultCount(long projectId, int resultCount) {
        this.projectId = projectId;
        this.resultCount = resultCount;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public int getResultCount() {
        return resultCount;
    }

    public void setResultCount(int resultCount) {
        this.resultCount = resultCount;
    }
}
