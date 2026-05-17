package com.bpcl.audit_portal.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApplicationResponse {

    private Long id;

    private String name;
}