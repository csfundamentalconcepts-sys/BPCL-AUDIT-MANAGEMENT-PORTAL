package com.bpcl.audit_portal.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldUpdateRequest {

    @NotBlank
    private String fieldName;

    @NotBlank
    private String newValue;
}
