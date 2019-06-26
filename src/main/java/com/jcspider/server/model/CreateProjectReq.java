package com.jcspider.server.model;

/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
public class CreateProjectReq extends Project {

    private static final long serialVersionUID = -588472126108791566L;
    private String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
