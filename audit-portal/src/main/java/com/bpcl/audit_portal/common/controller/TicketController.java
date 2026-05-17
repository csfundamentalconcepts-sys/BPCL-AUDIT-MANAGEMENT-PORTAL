package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.common.dto.CreateTicketRequest;
import com.bpcl.audit_portal.common.dto.TicketResponse;
import com.bpcl.audit_portal.common.dto.UpdateTicketRequest;
import com.bpcl.audit_portal.common.service.TicketService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public TicketResponse createTicket(
            @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal UserDetailsImplementation userDetails
    ) {

        return ticketService.createTicket(
                request,
                userDetails.getId()
        );
    }

    @PatchMapping("/{ticketId}")
    public TicketResponse updateTicket(
            @PathVariable Long ticketId,
            @RequestBody UpdateTicketRequest request,
            @AuthenticationPrincipal UserDetailsImplementation userDetails
    ) {

        return ticketService.updateTicket(
                ticketId,
                userDetails.getId(),
                request
        );
    }

    @GetMapping("/application/{applicationId}")
    public List<TicketResponse> getTicketsByApplication(
            @PathVariable Long applicationId
    ) {
        return ticketService.getTicketsByApplication(applicationId);
    }
}