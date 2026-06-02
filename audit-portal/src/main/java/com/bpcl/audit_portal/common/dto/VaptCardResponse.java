package com.bpcl.audit_portal.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VaptCardResponse {

    private Long id;

    private Long applicationId;

    private AuditInfoResponse auditInfo;
}