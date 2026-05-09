package com.bpcl.audit_portal.common.exceptions;

public class BAMPException extends RuntimeException {
    private final Errors error;

    public BAMPException(Errors error)  {
        super(error.getErrorMessage());
        this.error = error;
    }

    public Errors getError() {
        return error;
    }
}
