package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.common.dto.CreateTicketRequest;
import com.bpcl.audit_portal.common.dto.TicketResponse;
import com.bpcl.audit_portal.common.dto.TicketSummaryResponse;
import com.bpcl.audit_portal.common.dto.UpdateTicketRequest;
import com.bpcl.audit_portal.common.service.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('SCRUM_MASTER')")
    public TicketResponse createTicket(
            @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal UserDetailsImplementation userDetails) {
        return ticketService.createTicket(request, userDetails.getId());
    }

    @PostMapping("/{ticketId}/assign/{assignedToId}")
    @PreAuthorize("hasRole('SCRUM_MASTER')")
    public void assignTicket(
            @PathVariable Long ticketId,
            @PathVariable Long assignedToId,
            @AuthenticationPrincipal UserDetailsImplementation userDetails) {
        ticketService.assignTicket(ticketId, assignedToId, userDetails.getId());
    }

    @PostMapping("/{ticketId}/deassign")
    @PreAuthorize("hasRole('SCRUM_MASTER')")
    public void deassignTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserDetailsImplementation userDetails) {
        ticketService.deassignTicket(ticketId, userDetails.getId());
    }

    @PatchMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('SCRUM_MASTER','DEVELOPER')")
    public TicketResponse updateTicket(
            @PathVariable Long ticketId,
            @RequestBody UpdateTicketRequest request,
            @AuthenticationPrincipal UserDetailsImplementation userDetails) {

        return ticketService.updateTicket(
                ticketId,
                userDetails.getId(),
                request
        );
    }

    @GetMapping("/application/{applicationId}")
    public List<TicketResponse> getTicketsByApplication(
            @PathVariable Long applicationId) {
        return ticketService.getTicketsByApplication(applicationId);
    }

    @GetMapping("/application/{applicationId}/ticket_summary")
    public TicketSummaryResponse getTicketSummaryByApplication(
            @PathVariable Long applicationId) {
        return ticketService.getTicketSummaryByApplication(applicationId);
    }

    @GetMapping("/ticket_summary")
    public TicketSummaryResponse getTicketSummary() {
        return ticketService.getTicketSummary();
    }

    @GetMapping("/user/ticket_summary")
    public TicketSummaryResponse getUserTicketSummary(
            @AuthenticationPrincipal UserDetailsImplementation userDetails) {
        return ticketService.getTicketSummaryByUser(userDetails.getId());
    }

    @GetMapping("/user/{userId}/ticket_summary")
    public TicketSummaryResponse getTicketSummaryByUser(@PathVariable Long userId) {
        return ticketService.getTicketSummaryByUser(userId);
    }

    @GetMapping("{userId}/assigned-to")
    public List<TicketResponse> getTicketsByUser(@PathVariable Long userId) {
        return ticketService.getTicketsByUser(userId);
    }

    @GetMapping("/asigned-to")
    public List<TicketResponse> getAssignedTickets(@AuthenticationPrincipal UserDetailsImplementation userDetails) {
        return ticketService.getTicketsByUser(userDetails.getId());
    }
}