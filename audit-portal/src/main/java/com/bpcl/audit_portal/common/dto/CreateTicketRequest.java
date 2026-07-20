package com.bpcl.audit_portal.common.dto;

import com.bpcl.audit_portal.common.constants.Priority;
import com.bpcl.audit_portal.common.constants.TicketStatus;
import com.bpcl.audit_portal.common.constants.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTicketRequest {

    @NotBlank(message = "Title is mandatory")
    private String title;

    @NotBlank(message = "Description is mandatory")
    private String description;


    private Long assignedToId;

    private String startDate;
    private String targetDate;
    private String actualCompletionDate;
    private String deploymentDate;

    private Integer storyPoint;

    @NotNull(message = "Application ID is mandatory")
    private Long applicationId;

    private TicketStatus status;

    private TicketType type;

    @NotNull(message = "Priority is mandatory")
    private Priority priority;
}