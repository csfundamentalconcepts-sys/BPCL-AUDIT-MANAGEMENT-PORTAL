package com.bpcl.audit_portal.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVaptCardRequest {
    @NotNull
    private Long applicationId;
}