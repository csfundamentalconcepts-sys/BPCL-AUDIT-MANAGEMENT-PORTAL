package com.bpcl.audit_portal.common.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BAMPExceptionHandler{

    @ExceptionHandler(BAMPException.class)
    public ResponseEntity<?> handleBAMPException(BAMPException ex) {

        Errors error = ex.getError();
        return new ResponseEntity<>(error.getErrorMessage(), error.getHttpStatus());
    }
}