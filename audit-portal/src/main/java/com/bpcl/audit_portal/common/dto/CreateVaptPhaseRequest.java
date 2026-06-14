package com.bpcl.audit_portal.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateVaptPhaseRequest {

    @NotNull(message = "File is required.")
    private MultipartFile file;

    @NotBlank
    private String password;
}
