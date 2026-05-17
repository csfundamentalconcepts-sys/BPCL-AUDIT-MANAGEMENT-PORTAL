package com.bpcl.audit_portal.common.mapper;

import com.bpcl.audit_portal.common.dto.TicketResponse;
import com.bpcl.audit_portal.common.model.Ticket;

public class TicketDtoMapper {

    public static TicketResponse toDto(Ticket ticket) {
                return TicketResponse.builder()
                .id(ticket.getId())
                .description(ticket.getDescription())
                .assignedTo(ticket.getAssignedTo())
                .startDate(ticket.getStartDate())
                .targetDate(ticket.getTargetDate())
                .actualCompletionDate(ticket.getActualCompletionDate())
                .deploymentDate(ticket.getDeploymentDate())
                .storyPoint(ticket.getStoryPoint())
                .status(ticket.getStatus())
                .type(ticket.getType())
                .headComment(ticket.getHeadComment())
                .spocComment(ticket.getSpocComment())
                .build();
    }
}