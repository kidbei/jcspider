package com.jcspider.server.model;

public class JSONResult<T> {

    private boolean success;
    private String  msg;
    private T       data;

    public JSONResult() {
    }

    public JSONResult(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public JSONResult(boolean success, String msg, T data) {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    public JSONResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


    public static <T> JSONResult<T> success(T data) {
        return new JSONResult<>(true, data);
    }

    public static <T> JSONResult<T> error(String msg) {
        return new JSONResult<>(false, msg);
    }
}
