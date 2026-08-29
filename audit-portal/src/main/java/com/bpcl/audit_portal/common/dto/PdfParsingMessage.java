package com.bpcl.audit_portal.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PdfParsingMessage {

    private Long phaseId;

    private Long userId;

    private String blobName;

    private String password;
}
