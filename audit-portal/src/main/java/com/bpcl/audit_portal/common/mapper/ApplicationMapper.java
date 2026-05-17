package com.bpcl.audit_portal.common.mapper;

import com.bpcl.audit_portal.common.dto.ApplicationResponse;
import com.bpcl.audit_portal.common.model.Application;

public class ApplicationMapper {

    private ApplicationMapper() {
    }

    public static ApplicationResponse toDto(Application application) {

        return ApplicationResponse.builder()
                .id(application.getId())
                .name(application.getName())
                .build();
    }
}