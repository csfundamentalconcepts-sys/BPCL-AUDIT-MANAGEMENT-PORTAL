package com.bpcl.audit_portal.common.exceptions;

public class ErrorResponse {
    private long errorCode;
    private String message;

    public ErrorResponse(long errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public long getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}