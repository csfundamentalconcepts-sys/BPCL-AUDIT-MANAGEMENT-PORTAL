package com.bpcl.audit_portal.common.mapper;

import com.bpcl.audit_portal.common.dto.TicketAssignmentResponse;
import com.bpcl.audit_portal.common.model.TicketAssignment;

public class TicketAssignmentMapper {

    public static TicketAssignmentResponse toResponse(
            TicketAssignment assignment
    ) {

        return TicketAssignmentResponse.builder()
                .id(assignment.getId())

                .ticketId(
                        assignment.getTicket().getId()
                )

                .assignedToId(
                        assignment.getAssignedTo().getId()
                )
                .assignedToName(
                        assignment.getAssignedTo().getFullName()
                )

                .assignedById(
                        assignment.getAssignedBy().getId()
                )
                .assignedByName(
                        assignment.getAssignedBy().getFullName()
                )

                .active(
                        assignment.getActive()
                )

                .assignedAt(
                        assignment.getAssignedAt()
                )

                .deassignedAt(
                        assignment.getDeassignedAt()
                )

                .deassignedById(
                        assignment.getDeassignedBy() != null
                                ? assignment.getDeassignedBy().getId()
                                : null
                )

                .deassignedByName(
                        assignment.getDeassignedBy() != null
                                ? assignment.getDeassignedBy().getFullName()
                                : null
                )
                .build();
    }
}