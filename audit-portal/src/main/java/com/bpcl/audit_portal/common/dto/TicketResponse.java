package com.bpcl.audit_portal.common.dto;

import com.bpcl.audit_portal.common.constants.Priority;
import com.bpcl.audit_portal.common.constants.TicketStatus;
import com.bpcl.audit_portal.common.constants.TicketType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
public class TicketResponse {
    private Long id;

    private String title;

    private String description;

    private String assignedTo;

    private String startDate;

    private String targetDate;

    private String actualCompletionDate;

    private String deploymentDate;

    private Integer storyPoint;

    private TicketStatus status;

    private TicketType type;

    private String headComment;

    private String spocComment;

    private Priority priority;

    private LocalDateTime createdAt;

    private UserDto createdBy;

    private List<TicketHistoryResponse> history;
}
