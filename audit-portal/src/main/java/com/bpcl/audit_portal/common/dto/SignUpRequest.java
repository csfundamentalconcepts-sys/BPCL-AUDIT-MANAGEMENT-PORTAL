package com.bpcl.audit_portal.common.dto;

import com.bpcl.audit_portal.common.constants.AppRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SignUpRequest {

    @NotBlank
    @Email
    @Size(max = 150)
    private String username;

    @NotBlank
    @Size(min = 8, max = 40)
    private String password;

    @NotNull
    private AppRole role;

    @NotNull
    private Long assignedToUserId;

    private List<Long> applicationIds;
}