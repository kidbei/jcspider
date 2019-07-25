package com.jcspider.server.model;

import java.util.List;

/**
 * @author zhuang.hu
 * @since 06 June 2019
 */
public class DebugResult {

    private String  requestId;
    private boolean success;
    private String  stack;
    private String  currentMethod;
    private List<? extends SimpleTask> simpleTasks;
    private Object  result;
    private List<SelfLog>    logs;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStack() {
        return stack;
    }

    public void setStack(String stack) {
        this.stack = stack;
    }

    public String getCurrentMethod() {
        return currentMethod;
    }

    public void setCurrentMethod(String currentMethod) {
        this.currentMethod = currentMethod;
    }

    public List<? extends SimpleTask> getSimpleTasks() {
        return simpleTasks;
    }

    public void setSimpleTasks(List<? extends SimpleTask> simpleTasks) {
        this.simpleTasks = simpleTasks;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public List<SelfLog> getLogs() {
        return logs;
    }

    public void setLogs(List<SelfLog> logs) {
        this.logs = logs;
    }
}
