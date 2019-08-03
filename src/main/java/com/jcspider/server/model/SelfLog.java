package com.jcspider.server.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jcspider.server.utils.Constant;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author: Gosin
 * @Date: 2019-07-25 21:32
 */
public class SelfLog {

    private Long    id;
    @JsonSerialize(using = LongTimeFormat.Serialize.class)
    @JsonDeserialize(using = LongTimeFormat.Deserialize.class)
    private Long    logTime;
    private String  logText;
    private Boolean persistent;
    private Long    projectId;
    private String  taskId;
    private String  level;

    public SelfLog(String logText) {
        this(Constant.LEVEL_DEBUG, System.currentTimeMillis(), logText, false);
    }

    public SelfLog(String level, String logText) {
        this(level, System.currentTimeMillis(), logText, false);
    }


    public SelfLog(String logText, Boolean persistent) {
        this(Constant.LEVEL_DEBUG, System.currentTimeMillis(), logText, persistent);
    }

    public SelfLog(String level, Long logTime, String logText, Boolean persistent) {
        this.level = level;
        this.logTime = logTime;
        this.logText = logText;
        this.persistent = persistent;
    }

    public Long getLogTime() {
        return logTime;
    }

    public void setLogTime(Long logTime) {
        this.logTime = logTime;
    }

    public String getLogText() {
        return logText;
    }

    public void setLogText(String logText) {
        this.logText = logText;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getPersistent() {
        return persistent;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss");
        return "[" + this.level + "] " + sdf.format(new Date(this.logTime)) + " " + this.logText;
    }
}
