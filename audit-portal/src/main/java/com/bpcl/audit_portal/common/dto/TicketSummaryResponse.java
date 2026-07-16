package com.bpcl.audit_portal.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketSummaryResponse {

    private Long totalTickets;

    private Long inProgressTickets;

    private Long notStartedTickets;

    private Long holdTickets;

    private Long closedTickets;
}