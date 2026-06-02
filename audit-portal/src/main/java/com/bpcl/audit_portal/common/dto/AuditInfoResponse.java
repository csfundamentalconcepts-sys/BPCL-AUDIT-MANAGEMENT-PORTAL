package com.bpcl.audit_portal.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditInfoResponse {

    private Long userId;

    private String username;

    private LocalDateTime createdAt;
}
