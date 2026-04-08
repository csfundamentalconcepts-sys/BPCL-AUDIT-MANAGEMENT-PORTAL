package com.bpcl.audit_portal.dto;

public record IssueDto(
        Integer serialNo,
        String affectedAsset,
        String observationTitle,
        String cveCwe,
        String severity,
        String recommendationReference,
        String reportSection,
        String observationType,
        String status,
        String assessmentRemarks
) {
}