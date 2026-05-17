package com.bpcl.audit_portal.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateTicketRequest {
    private List<TicketFieldUpdateRequest> updates;
}
