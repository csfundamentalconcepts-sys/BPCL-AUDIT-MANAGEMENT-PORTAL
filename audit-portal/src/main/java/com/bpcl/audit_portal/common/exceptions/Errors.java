package com.bpcl.audit_portal.common.exceptions;

import org.springframework.http.HttpStatus;

public enum Errors {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 1001L,
            "Your session token is invalid. Please log in again."),

    INTERNAL_ISSUE(HttpStatus.INTERNAL_SERVER_ERROR, 1002L,
            "Something went wrong on our side. Please try again later."),

    USER_NOT_REGISTERED(HttpStatus.NOT_FOUND, 1003L,
            "No account found for given credentials."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 1004L,
            "The requested user could not be found."),

    APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, 1019L,
            "Application not found."),

    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, 1020L,
            "Ticket not found."),

    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, 1005L,
            "The specified role does not exist."),

    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 1006L,
            "Your session has expired. Please log in again."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 1016L,
            "You are not authorized to access this resource. Please log in."),

    INVALID_PERMISSIONS(HttpStatus.BAD_REQUEST, 1017L,
            "One or more provided permissions are invalid."),

    INVALID_FIELD_NAME(HttpStatus.BAD_REQUEST, 1017L,
            " field name is invalid."),

    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, 1007L,
            "Refresh token not found. Please authenticate again."),

    MAIL_SEND_FAILING(HttpStatus.INTERNAL_SERVER_ERROR, 1008L,
            "Unable to send email at the moment. Please try again later."),

    USERNAME_ALREADY_IN_USE(HttpStatus.CONFLICT, 1011L,
            "This username is already taken."),

    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, 1012L,
            "You do not have permission to perform this action."),

    INVALID_PASSWORD_RESET_TOKEN(HttpStatus.BAD_REQUEST, 1013L,
            "Password reset failed"),

    PASSWORD_RESET_TOKEN_ALREADY_USED(HttpStatus.BAD_REQUEST, 1014L,
            "Password reset failed"),

    INVALID_ASSIGNMENT(HttpStatus.BAD_REQUEST, 1018L,
            "Invalid assignment based on role hierarchy."),

    PASSWORD_RESET_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, 1015L,
            "Password reset failed"),

    MANDATORY_FIELD_MISSING(HttpStatus.BAD_REQUEST, 1021L,
            "Mandatory field is missing."),

    INVALID_STATUS(HttpStatus.BAD_REQUEST, 1022L,
            "Invalid status value."),

    INVALID_PRIORITY(HttpStatus.BAD_REQUEST, 1032L,
            "Invalid priority value."),

    INVALID_TYPE(HttpStatus.BAD_REQUEST, 1023L,
            "Invalid type value."),

    INVALID_STORY_POINT(HttpStatus.BAD_REQUEST, 1023L,
            "Story point must be a valid number."),

    APPLICATION_ALREADY_EXISTS(HttpStatus.CONFLICT, 1024L,
            "Application already exists."),
    VAPT_CARD_ALREADY_EXISTS(HttpStatus.CONFLICT, 1025L,
            "VAPT Card already exists for this application."),

    VAPT_CARD_NOT_FOUND(HttpStatus.NOT_FOUND, 1026L,
            "VAPT Card not found."),

    VAPT_AUDIT_ALREADY_EXISTS(HttpStatus.CONFLICT, 1027L,
            "VAPT already exists for this year."),

    VAPT_AUDIT_NOT_FOUND(HttpStatus.NOT_FOUND, 1028L,
            "VAPT Audit not found."),

    VAPT_PHASE_ALREADY_EXISTS(HttpStatus.CONFLICT, 1029L,
            "Phase already exists for this audit."),

    PREVIOUS_PHASE_NOT_FOUND(HttpStatus.BAD_REQUEST, 1030L,
            "Previous phase does not exist."),

    PREVIOUS_PHASE_NOT_COMPLETED(HttpStatus.BAD_REQUEST, 1031L,
            "Previous phase must be completed before creating a new one."),

    VULNERABILITY_NOT_FOUND(HttpStatus.NOT_FOUND, 1032L,
            "Vulnerability not found."),

    VAPT_PHASE_NOT_FOUND(HttpStatus.NOT_FOUND, 1033L,
            "VAPT audit phase not found."),

    VAPT_PHASE_CANNOT_BE_CLOSED(HttpStatus.BAD_REQUEST, 1034L,
            "All OPEN vulnerabilities must be FIXED before closing phase."),

    VAPT_AUDIT_CANNOT_BE_CLOSED(HttpStatus.BAD_REQUEST, 1035L,
            "All vulnerabilities of the last phase must be CLOSED before closing the audit."),

    USER_ALREADY_ASSIGNED(
            HttpStatus.CONFLICT,
            1036L,
            "User is already assigned."),

    USER_ASSIGNMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            1037L,
            "User assignment not found."),

    INVALID_USER_HIERARCHY(
            HttpStatus.BAD_REQUEST,
            1038L,
            "Invalid hierarchy assignment."
    ),
    APPLICATION_ALREADY_ASSIGNED(
            HttpStatus.CONFLICT,
            1039L,
            "Application already assigned."
    ),
    APPLICATION_NOT_ASSIGNED(
            HttpStatus.BAD_REQUEST,
            1040L,
            "Application is not assigned."
    ),
    INVALID_VULNERABILITY_ASSIGNMENT(
            HttpStatus.BAD_REQUEST,
            1034L,
            "Only Scrum Master can assign vulnerabilities to developers."
    ),

    INVALID_TICKET_ASSIGNMENT(
            HttpStatus.BAD_REQUEST,
            1035L,
            "Only Scrum Master can assign tickets to developers."
    ),VULNERABILITY_NOT_ASSIGNED(
            HttpStatus.BAD_REQUEST,
            1041L,
            "Vulnerability is not assigned."
    ),
    TICKET_NOT_ASSIGNED(
            HttpStatus.BAD_REQUEST,
            1042L,
            "Ticket is not assigned."
    ),
    TICKET_ALREADY_ASSIGNED(
            HttpStatus.BAD_REQUEST,
            1043L,
            "Ticket already assigned.");

    private final HttpStatus httpStatus;
    private final Long errorCode;
    private final String errorMessage;

    Errors(HttpStatus httpStatus, Long errorCode, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Long getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}