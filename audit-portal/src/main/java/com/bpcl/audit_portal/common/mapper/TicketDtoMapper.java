package com.bpcl.audit_portal.common.mapper;

import com.bpcl.audit_portal.common.dto.TicketResponse;
import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.model.Ticket;
import org.springframework.stereotype.Component;

@Component
public class TicketDtoMapper {


    public  static TicketResponse toDto(Ticket ticket) {
        UserDto createdBy = UserDtoMapper.toDto(ticket.getCreatedBy());
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
                .createdAt(ticket.getCreatedAt())
                .createdBy(createdBy)
                .priority(ticket.getPriority())
                .build();
    }
}