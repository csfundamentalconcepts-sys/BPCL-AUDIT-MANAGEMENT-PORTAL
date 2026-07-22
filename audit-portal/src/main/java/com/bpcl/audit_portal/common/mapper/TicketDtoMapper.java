package com.bpcl.audit_portal.common.mapper;

import com.bpcl.audit_portal.common.dto.TicketResponse;
import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.model.Ticket;
import com.bpcl.audit_portal.common.model.TicketAssignment;

public class TicketDtoMapper {

    public static TicketResponse toDto(
            Ticket ticket,
            TicketAssignment assignment
    ) {

        if (ticket == null) {
            return null;
        }

        UserDto createdBy =
                UserDtoMapper.toDto(
                        ticket.getCreatedBy()
                );

        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())

                .startDate(ticket.getStartDate())
                .targetDate(ticket.getTargetDate())
                .actualCompletionDate(
                        ticket.getActualCompletionDate()
                )
                .deploymentDate(
                        ticket.getDeploymentDate()
                )

                .storyPoint(ticket.getStoryPoint())

                .status(ticket.getStatus())
                .type(ticket.getType())

                .headComment(ticket.getHeadComment())
                .spocComment(ticket.getSpocComment())

                .createdBy(createdBy)

                .assignedUserId(
                        assignment != null
                                ? assignment.getAssignedTo().getId()
                                : null
                )
                .assignedUserName(
                        assignment != null
                                ? assignment.getAssignedTo().getFullName()
                                : null
                )

                .createdAt(ticket.getCreatedAt())
                .build();
    }
}