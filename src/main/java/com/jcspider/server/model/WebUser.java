package com.jcspider.server.model;


/**
 * @author zhuang.hu
 * @since 13 June 2019
 */
public class WebUser {

    private Long    id;
    private String  uid;
    private String  cnName;
    private String  role;
    private String  password;
    private String  token;
    private String  inviteUid;
    private Long   createdAt;
    private Long   updatedAt;
    private Long   tokenCreatedAt;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCnName() {
        return cnName;
    }

    public void setCnName(String cnName) {
        this.cnName = cnName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getInviteUid() {
        return inviteUid;
    }

    public void setInviteUid(String inviteUid) {
        this.inviteUid = inviteUid;
    }

    public Long getTokenCreatedAt() {
        return tokenCreatedAt;
    }

    public void setTokenCreatedAt(Long tokenCreatedAt) {
        this.tokenCreatedAt = tokenCreatedAt;
    }
}
