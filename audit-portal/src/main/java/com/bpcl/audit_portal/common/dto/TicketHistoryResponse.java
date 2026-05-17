package com.bpcl.audit_portal.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TicketHistoryResponse {

    private String fieldName;

    private String oldValue;

    private String newValue;

    private String updatedBy;

    private LocalDateTime updatedAt;
}