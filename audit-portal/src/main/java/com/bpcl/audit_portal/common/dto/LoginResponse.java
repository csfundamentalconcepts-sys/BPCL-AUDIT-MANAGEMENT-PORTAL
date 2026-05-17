package com.bpcl.audit_portal.common.dto;

public class LoginResponse {
    private String jwtToken;

    private String userName;

    private String role;

    public LoginResponse(String userName, String role, String jwtToken) {
        this.userName = userName;
        this.role = role;
        this.jwtToken = jwtToken;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoles() {
        return role;
    }

    public void setRoles(String role) {
        this.role = role;
    }
}