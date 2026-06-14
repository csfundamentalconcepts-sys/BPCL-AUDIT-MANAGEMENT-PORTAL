package com.bpcl.audit_portal.common.dto;

import com.bpcl.audit_portal.common.constants.VaptPhaseStatus;
import com.bpcl.audit_portal.common.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VaptAuditPhaseResponses {

    private Long id;

    private Integer phaseNumber;

    private VaptPhaseStatus status;

    private User createdBy;

    private LocalDateTime createdAt;
}
