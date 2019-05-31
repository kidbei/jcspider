package com.jcspider.server.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class Result implements Serializable {

    private String  uuid;
    private String  taskId;
    private String  resultText;
    private Map<String,Object> result;
    private Timestamp   createdAt;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
