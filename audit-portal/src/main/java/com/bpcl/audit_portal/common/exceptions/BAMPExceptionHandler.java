package com.bpcl.audit_portal.common.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BAMPExceptionHandler{

    @ExceptionHandler(BAMPException.class)
    public ResponseEntity<ErrorResponse> handleBAMPException(BAMPException ex) {

        Errors error = ex.getError();

        ErrorResponse response = new ErrorResponse(
                error.getErrorCode(),
                error.getErrorMessage()
        );
        return new ResponseEntity<>(response, error.getHttpStatus());
    }
}