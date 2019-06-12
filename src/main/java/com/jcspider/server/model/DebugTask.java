package com.jcspider.server.model;

public class DebugTask {

    private String  requestId;
    private String  scriptText;
    private SimpleTask  simpleTask;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(String scriptText) {
        this.scriptText = scriptText;
    }

    public SimpleTask getSimpleTask() {
        return simpleTask;
    }

    public void setSimpleTask(SimpleTask simpleTask) {
        this.simpleTask = simpleTask;
    }

}
