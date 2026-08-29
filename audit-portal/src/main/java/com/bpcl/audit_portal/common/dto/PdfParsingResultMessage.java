package com.bpcl.audit_portal.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfParsingResultMessage {

    private Long phaseId;

    private Long userId;

    private String blobName;

    private String password;

    private List<Map<String, Object>> parsed;
}
