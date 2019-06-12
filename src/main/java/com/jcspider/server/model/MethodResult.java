package com.jcspider.server.model;

import java.util.Map;

/**
 * @author zhuang.hu
 * @since 31 May 2019
 */
public class MethodResult {

    private FindTask findTask;
    private Map<String, Object> result;

    public MethodResult() {
    }

    public MethodResult(FindTask findTask, Map<String, Object> result) {
        this.findTask = findTask;
        this.result = result;
    }

    public FindTask getFindTask() {
        return findTask;
    }

    public void setFindTask(FindTask findTask) {
        this.findTask = findTask;
    }

    public Map<String, Object> getResult() {
        return result;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }
}
