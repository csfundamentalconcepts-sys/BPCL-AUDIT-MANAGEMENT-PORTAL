package com.bpcl.audit_portal.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVaptAuditRequest {

    @NotNull
    private Long cardId;
    @NotNull
    private Integer auditYear;
}
