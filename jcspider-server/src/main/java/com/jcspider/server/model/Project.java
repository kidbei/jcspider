package com.jcspider.server.model;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class Project implements Serializable {

    private long    id;
    private String  name;
    private String  startUrl;
    private String  scriptText;
    private String  status;
    private String  rateUnit;
    private int     rateNumber;
    private String  dispatcher;
    private Timestamp   createdAt;
    private Timestamp   updatedAt;
    private String  scheduleType;
    private long  scheduleValue;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public String getRateUnit() {
        return rateUnit;
    }

    public void setRateUnit(String rateUnit) {
        this.rateUnit = rateUnit;
    }

    public int getRateNumber() {
        return rateNumber;
    }

    public void setRateNumber(int rateNumber) {
        this.rateNumber = rateNumber;
    }

    public String getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(String dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }

    public long getScheduleValue() {
        return scheduleValue;
    }

    public void setScheduleValue(long scheduleValue) {
        this.scheduleValue = scheduleValue;
    }
}
