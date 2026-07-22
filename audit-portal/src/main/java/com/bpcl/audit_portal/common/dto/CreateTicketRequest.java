package com.bpcl.audit_portal.common.dto;

import com.bpcl.audit_portal.common.constants.Priority;
import com.bpcl.audit_portal.common.constants.TicketStatus;
import com.bpcl.audit_portal.common.constants.TicketType;
import lombok.Data;

@Data
public class CreateTicketRequest {

    private String title;
    private String description;
    private Priority priority;
    private TicketStatus status;
    private TicketType type;

    private String startDate;
    private String targetDate;
    private String actualCompletionDate;
    private String deploymentDate;

    private Integer storyPoint;

    private Long applicationId;

    private Long assignedToId;
}