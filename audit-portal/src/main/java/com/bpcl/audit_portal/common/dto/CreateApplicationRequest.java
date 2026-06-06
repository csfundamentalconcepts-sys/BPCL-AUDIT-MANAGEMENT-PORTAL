package com.bpcl.audit_portal.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateApplicationRequest {

    @NotBlank(message = "Application name is required")
    private String name;
}