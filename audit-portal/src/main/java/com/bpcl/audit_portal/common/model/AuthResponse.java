package com.bpcl.audit_portal.common.model;

import com.bpcl.audit_portal.auth.model.RefreshToken;
import lombok.Data;

@Data
public class AuthResponse {

    private LoginResponse loginResponse;
    private RefreshToken refreshToken;

    public AuthResponse(LoginResponse loginResponse, RefreshToken refreshToken) {
        this.loginResponse = loginResponse;
        this.refreshToken = refreshToken;
    }
}
