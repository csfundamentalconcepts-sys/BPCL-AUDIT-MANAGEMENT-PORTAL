package com.bpcl.audit_portal.common.dto;

import lombok.Data;

@Data
public class CreateVaptAuditRequest {
    private Long cardId;
    private Integer auditYear;
}
