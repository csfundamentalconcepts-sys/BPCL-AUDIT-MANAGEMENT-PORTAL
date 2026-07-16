package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.dto.CreateTicketRequest;
import com.bpcl.audit_portal.common.dto.TicketResponse;
import com.bpcl.audit_portal.common.dto.TicketSummaryResponse;
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

        AppRole role = userDetails.getAuthorities()
                .stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring("ROLE_".length()))
                .map(AppRole::valueOf)
                .findFirst()
                .orElse(null);

        return ticketService.updateTicket(
                ticketId,
                userDetails.getId(),
                role,
                request
        );
    }

    @GetMapping("/application/{applicationId}")
    public List<TicketResponse> getTicketsByApplication(
            @PathVariable Long applicationId
    ) {
        return ticketService.getTicketsByApplication(applicationId);
    }
    @GetMapping("/application/{applicationId}/summary")
    public TicketSummaryResponse getTicketSummary(
            @PathVariable Long applicationId
    ) {
        return ticketService.getTicketSummaryByApplication(applicationId);
    }

    @GetMapping("/summary")
    public TicketSummaryResponse getTicketSummary() {
        return ticketService.getTicketSummary();
    }
}