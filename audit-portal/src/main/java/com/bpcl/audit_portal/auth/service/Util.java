package com.bpcl.audit_portal.auth.service;

import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;

public class Util {

    public static void validateRoleCreation(
            AppRole creatorRole,
            AppRole targetRole) {

        switch (creatorRole) {

            case ADMIN -> {
                if (targetRole != AppRole.HEAD) {
                    throw new BAMPException(Errors.UNAUTHORIZED);
                }
            }

            case HEAD -> {
                if (targetRole != AppRole.SPOC) {
                    throw new BAMPException(Errors.UNAUTHORIZED);
                }
            }

            case SPOC -> {
                if (targetRole != AppRole.SCRUM_MASTER) {
                    throw new BAMPException(Errors.UNAUTHORIZED);
                }
            }

            case SCRUM_MASTER -> {
                if (targetRole != AppRole.DEVELOPER) {
                    throw new BAMPException(Errors.UNAUTHORIZED);
                }
            }

            default -> throw new BAMPException(Errors.UNAUTHORIZED);
        }
    }
    public static void validateUserAssignment(
            AppRole parentRole,
            AppRole childRole
    ) {

        switch (parentRole) {

            case ADMIN -> {
                if (childRole != AppRole.HEAD) {
                    throw new BAMPException(
                            Errors.INVALID_USER_HIERARCHY
                    );
                }
            }

            case HEAD -> {
                if (childRole != AppRole.SPOC) {
                    throw new BAMPException(
                            Errors.INVALID_USER_HIERARCHY
                    );
                }
            }

            case SPOC -> {
                if (childRole != AppRole.SCRUM_MASTER) {
                    throw new BAMPException(
                            Errors.INVALID_USER_HIERARCHY
                    );
                }
            }

            case SCRUM_MASTER -> {
                if (childRole != AppRole.DEVELOPER) {
                    throw new BAMPException(
                            Errors.INVALID_USER_HIERARCHY
                    );
                }
            }

            default ->
                    throw new BAMPException(
                            Errors.INVALID_USER_HIERARCHY
                    );
        }
    }
}