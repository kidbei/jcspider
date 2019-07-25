package com.jcspider.server.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: Gosin
 * @Date: 2019-07-25 21:32
 */
public class SelfLog {

    private long    logTime;
    private String  logText;
    private boolean persistent;
    private long    projectId;
    private String  taskId;

    public SelfLog(String logText) {
        this(System.currentTimeMillis(), logText, false);
    }

    public SelfLog(String logText, boolean persistent) {
        this(System.currentTimeMillis(), logText, persistent);
    }

    public SelfLog(long logTime, String logText, boolean persistent) {
        this.logTime = logTime;
        this.logText = logText;
        this.persistent = persistent;
    }

    public long getLogTime() {
        return logTime;
    }

    public void setLogTime(long logTime) {
        this.logTime = logTime;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
        return sdf.format(new Date(this.logTime)) + " " + this.logText;
    }
}
