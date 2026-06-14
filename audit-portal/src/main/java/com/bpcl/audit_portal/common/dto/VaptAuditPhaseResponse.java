package com.bpcl.audit_portal.common.dto;

import com.bpcl.audit_portal.common.constants.VaptPhaseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VaptAuditPhaseResponse {

    private Long id;

    private Integer phaseNumber;

    private VaptPhaseStatus status;

    private UserDto createdBy;

    private LocalDateTime createdAt;
}
