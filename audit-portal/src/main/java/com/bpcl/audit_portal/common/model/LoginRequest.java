package com.bpcl.audit_portal.common.model;

public class LoginRequest {
    private String userName;

    private String password;

    public String getUserName(){
        return userName;
    }

    public void setUsername(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

