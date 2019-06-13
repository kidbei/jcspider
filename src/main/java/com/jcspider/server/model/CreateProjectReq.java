package com.jcspider.server.model;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
public class CreateProjectReq extends Project {

    private String  uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
