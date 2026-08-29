package com.bpcl.audit_portal.common.dto;

import com.bpcl.audit_portal.common.constants.ParsingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParsingInPrgressResponse {
     Long phaseId;
     String message;
     ParsingStatus status;
}
