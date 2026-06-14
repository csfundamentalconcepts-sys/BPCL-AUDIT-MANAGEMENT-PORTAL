package com.bpcl.audit_portal.common.mapper;

import com.bpcl.audit_portal.common.dto.VaptAuditPhaseResponse;
import com.bpcl.audit_portal.common.model.VaptAuditPhase;

public class VaptAuditPhaseMapper {

    public static VaptAuditPhaseResponse toResponse(VaptAuditPhase vaptAuditPhase) {

        if (vaptAuditPhase == null) {
            return null;
        }

        return VaptAuditPhaseResponse.builder()
                .id(vaptAuditPhase.getId())
                .phaseNumber(vaptAuditPhase.getPhaseNumber())
                .status(vaptAuditPhase.getStatus())
                .createdBy(UserDtoMapper.toDto(vaptAuditPhase.getCreatedBy()))
                .createdAt(vaptAuditPhase.getCreatedAt())
                .build();
    }
}