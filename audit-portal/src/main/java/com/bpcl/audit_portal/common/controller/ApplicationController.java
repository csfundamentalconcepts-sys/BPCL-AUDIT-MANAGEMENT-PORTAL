package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.common.dto.ApplicationResponse;
import com.bpcl.audit_portal.common.dto.CreateApplicationRequest;
import com.bpcl.audit_portal.common.service.ApplicationService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(
            ApplicationService applicationService
    ) {
        this.applicationService = applicationService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApplicationResponse createApplication(
            @RequestBody CreateApplicationRequest request,
            @AuthenticationPrincipal UserDetailsImplementation userDetails
    ) {

        return applicationService.createApplication(
                request,
                userDetails.getId()
        );
    }

    @PostMapping("/{applicationId}/assign/{userId}")
    public void assignApplication(
            @PathVariable Long applicationId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImplementation currentUser
    ) {

        applicationService.assignApplication(
                applicationId,
                userId,
                currentUser.getId()
        );
    }

    @DeleteMapping("/{applicationId}/de-assign/{userId}")
    public void deassignApplication(
            @PathVariable Long applicationId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImplementation currentUser
    ) {

        applicationService.deassignApplication(
                applicationId,
                userId,
                currentUser.getId()
        );
    }

    @GetMapping("/assigned")
    public List<ApplicationResponse> getAssignedApplications(
            @AuthenticationPrincipal UserDetailsImplementation currentUser
    ) {

        return applicationService.getAssignedApplications(
                currentUser.getId()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ApplicationResponse> getAllApplications() {

        return applicationService.getAllApplications();
    }
}