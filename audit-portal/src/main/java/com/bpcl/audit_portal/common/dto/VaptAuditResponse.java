package com.bpcl.audit_portal.common.dto;

import com.bpcl.audit_portal.common.constants.VaptAuditStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VaptAuditResponse {

    private Long id;

    private Long cardId;

    private Integer auditYear;

    private VaptAuditStatus status;

    private AuditInfoResponse auditInfo;
}