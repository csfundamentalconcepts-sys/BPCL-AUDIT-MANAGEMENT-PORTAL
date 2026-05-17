package com.bpcl.audit_portal.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketFieldUpdateRequest {

    private String fieldName;

    private String newValue;
}
