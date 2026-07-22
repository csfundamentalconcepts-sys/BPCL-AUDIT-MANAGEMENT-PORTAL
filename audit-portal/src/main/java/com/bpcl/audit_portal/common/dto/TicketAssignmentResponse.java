package com.bpcl.audit_portal.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketAssignmentResponse {

    private Long id;

    private Long ticketId;

    private Long assignedToId;
    private String assignedToName;

    private Long assignedById;
    private String assignedByName;

    private Boolean active;

    private LocalDateTime assignedAt;
    private LocalDateTime deassignedAt;

    private Long deassignedById;
    private String deassignedByName;
}