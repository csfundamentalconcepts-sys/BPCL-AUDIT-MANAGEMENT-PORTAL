package com.bpcl.audit_portal.common.dto;

import com.bpcl.audit_portal.common.constants.Priority;
import com.bpcl.audit_portal.common.constants.TicketStatus;
import com.bpcl.audit_portal.common.constants.TicketType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TicketResponse {

    private Long id;

    private String title;

    private Priority priority;

    private String description;

    private String startDate;

    private String targetDate;

    private String actualCompletionDate;

    private String deploymentDate;

    private Integer storyPoint;

    private TicketStatus status;

    private TicketType type;

    private String headComment;

    private String spocComment;

    private UserDto createdBy;

    private Long assignedUserId;

    private String assignedUserName;

    private LocalDateTime createdAt;
}