package com.bpcl.audit_portal.auth.service;

import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;

public class Util {

    public static void validateRoleCreation(AppRole creatorRole, AppRole targetRole) {

        switch (creatorRole) {
            case ADMIN -> {
                if (targetRole != AppRole.HEAD && targetRole != AppRole.ADMIN) {
                    throw new BAMPException(Errors.UNAUTHORIZED);
                }
            }
            case HEAD -> {
                if (!(targetRole == AppRole.SPOC ||
                        targetRole == AppRole.DEVELOPER ||
                        targetRole == AppRole.SCRUM_MASTER)) {
                    throw new BAMPException(Errors.UNAUTHORIZED);
                }
            }
            case SPOC -> {
                if (!(targetRole == AppRole.DEVELOPER ||
                        targetRole == AppRole.SCRUM_MASTER)) {
                    throw new BAMPException(Errors.UNAUTHORIZED);
                }
            }
            default -> throw new BAMPException(Errors.UNAUTHORIZED);
        }
    }

    public static void validateAssignment(AppRole targetRole, AppRole assignedToRole) {

        switch (targetRole) {
            case HEAD -> {
                if (assignedToRole != AppRole.ADMIN) {
                    throw new BAMPException(Errors.INVALID_ASSIGNMENT);
                }
            }
            case SPOC -> {
                if (assignedToRole != AppRole.HEAD) {
                    throw new BAMPException(Errors.INVALID_ASSIGNMENT);
                }
            }
            case DEVELOPER, SCRUM_MASTER -> {
                if (!(assignedToRole == AppRole.HEAD ||
                        assignedToRole == AppRole.SPOC)) {
                    throw new BAMPException(Errors.INVALID_ASSIGNMENT);
                }
            }
            default -> throw new BAMPException(Errors.INVALID_ASSIGNMENT);
        }
    }

}