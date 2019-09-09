package com.jcspider.server.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class Project implements Serializable {

    private static final long serialVersionUID = -744816240911656974L;
    private Long id;
    private String      name;
    private String      startUrl;
    private String      scriptText;
    private String      status;
    private String      dispatcher;
    @JsonSerialize(using = LongTimeFormat.Serialize.class)
    @JsonDeserialize(using = LongTimeFormat.Deserialize.class)
    private Long        createdAt;
    @JsonSerialize(using = LongTimeFormat.Serialize.class)
    @JsonDeserialize(using = LongTimeFormat.Deserialize.class)
    private Long        updatedAt;
    private String      scheduleType;
    private Long        scheduleValue;
    private String      description;
    private Double      qps;


    private Integer     resultCount;

    public Project() {
    }

    public Project(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartUrl() {
        return startUrl;
    }

    public void setStartUrl(String startUrl) {
        this.startUrl = startUrl;
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(String scriptText) {
        this.scriptText = scriptText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(String dispatcher) {
        this.dispatcher = dispatcher;
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

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public Long getScheduleValue() {
        return scheduleValue;
    }

    public void setScheduleValue(Long scheduleValue) {
        this.scheduleValue = scheduleValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getResultCount() {
        return resultCount;
    }

    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }

    public Double getQps() {
        return qps;
    }

    public void setQps(Double qps) {
        this.qps = qps;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", startUrl='" + startUrl + '\'' +
                ", scriptText='" + scriptText + '\'' +
                ", status='" + status + '\'' +
                ", dispatcher='" + dispatcher + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", scheduleType='" + scheduleType + '\'' +
                ", qps='" + qps + '\'' +
                ", scheduleValue=" + scheduleValue +
                ", description='" + description + '\'' +
                '}';
    }
}
