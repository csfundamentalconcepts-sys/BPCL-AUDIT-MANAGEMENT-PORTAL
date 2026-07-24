package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.common.dto.ApplicationResponse;
import com.bpcl.audit_portal.common.dto.CreateApplicationRequest;
import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.service.ApplicationService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> assignApplication(
            @PathVariable Long applicationId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImplementation currentUser
    ) {

        applicationService.assignApplication(
                applicationId,
                userId,
                currentUser.getId()
        );
        return ResponseEntity.ok("Successfully Assigned!");
    }

    @DeleteMapping("/{applicationId}/de-assign/{userId}")
    public ResponseEntity<?> deassignApplication(
            @PathVariable Long applicationId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImplementation currentUser
    ) {

        applicationService.deassignApplication(
                applicationId,
                userId,
                currentUser.getId()
        );
        return ResponseEntity.ok("Successfully Deassigned!");
    }

    @GetMapping("/assigned")
    public List<ApplicationResponse> getAssignedApplications(
            @AuthenticationPrincipal UserDetailsImplementation currentUser
    ){
        return applicationService.getAssignedApplications(
                currentUser.getId()
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ApplicationResponse> getAllApplications() {

        return applicationService.getAllApplications();
    }

    @GetMapping("/{applicationId}/users-assigned-to-application")
    public List<UserDto> getAllUsersAssignedToApplication(@PathVariable Long applicationId) {

        return applicationService.getAllUsersAssignedToApplication(applicationId);
    }
}