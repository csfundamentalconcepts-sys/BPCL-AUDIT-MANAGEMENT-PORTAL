package com.bpcl.audit_portal.common.dto;

import com.bpcl.audit_portal.common.constants.TicketStatus;
import com.bpcl.audit_portal.common.constants.TicketType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateTicketRequest {

    private String description;

    private String assignedTo;

    private String startDate;

    private String targetDate;

    private String actualCompletionDate;

    private String deploymentDate;

    private Integer storyPoint;

    private Long applicationId;

    private TicketStatus status;

    private TicketType type;
}