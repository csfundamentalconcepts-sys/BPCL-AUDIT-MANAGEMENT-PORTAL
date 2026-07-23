package com.bpcl.audit_portal.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class CweStatsResponse {

    private String cveCwe;
    private Long count;
}