package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.common.dto.TicketHistoryResponse;
import com.bpcl.audit_portal.common.service.TicketHistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket-history")
public class TicketHistoryController {

    private final TicketHistoryService ticketHistoryService;

    public TicketHistoryController(TicketHistoryService ticketHistoryService) {
        this.ticketHistoryService = ticketHistoryService;
    }

    @GetMapping("/{ticketId}")
    public List<TicketHistoryResponse> getTicketHistory(
            @PathVariable Long ticketId
    ) {
        return ticketHistoryService.getTicketHistory(ticketId);
    }
}