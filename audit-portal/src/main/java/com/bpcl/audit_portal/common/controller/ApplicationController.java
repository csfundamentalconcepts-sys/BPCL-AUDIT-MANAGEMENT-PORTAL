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
    @PreAuthorize("hasAnyRole('SPOC','HEAD','ADMIN')")
    public ApplicationResponse createApplication(
            @RequestBody CreateApplicationRequest request,
            @AuthenticationPrincipal UserDetailsImplementation userDetails
    ) {

        return applicationService.createApplication(
                request,
                userDetails.getId()
        );
    }

    @GetMapping("/assigned")
    public List<ApplicationResponse> getAllAssignedApplications(
            @AuthenticationPrincipal UserDetailsImplementation userDetails
    ) {
        return applicationService.getAllAssignedApplications(
                userDetails.getId()
        );
    }
    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public List<ApplicationResponse> getAllApplications() {
        return applicationService.getAllApplications();
    }

}
