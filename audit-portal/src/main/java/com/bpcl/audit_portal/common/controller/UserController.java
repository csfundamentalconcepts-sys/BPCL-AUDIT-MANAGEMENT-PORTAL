package com.bpcl.audit_portal.common.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.auth.service.AuthService;
import com.bpcl.audit_portal.common.dto.SignUpRequest;
import com.bpcl.audit_portal.common.dto.UserAssignmentRequest;
import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(
            UserService userService,
            AuthService authService
    ) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping
    public UserDto createUser(
            @RequestBody SignUpRequest request,
            @AuthenticationPrincipal UserDetailsImplementation userDetails
    ) {
        return authService.registerUser(
                request,
                userDetails
        );
    }

    @GetMapping("/parents")
    public List<UserDto> parentUsers(
            @AuthenticationPrincipal
            UserDetailsImplementation userDetails) {

        return userService.getParentUsers(
                userDetails.getId()
        );
    }

    @GetMapping("/children")
    public List<UserDto> childUsers(
            @AuthenticationPrincipal
            UserDetailsImplementation userDetails) {

        return userService.getChildUsers(
                userDetails.getId()
        );
    }
    @GetMapping("/reporting-to-me")
    public List<UserDto> reportingToMe(
            @AuthenticationPrincipal
            UserDetailsImplementation userDetails) {

        return userService.getReportingToMe(
                userDetails.getId()
        );
    }
    @PostMapping("/assign")
    public ResponseEntity<Void> assignUser(
            @RequestBody UserAssignmentRequest request,
            @AuthenticationPrincipal
            UserDetailsImplementation userDetails
    ) {

        userService.assignUser(
                userDetails.getId(),
                request.getChildUserId()
        );

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{childUserId}/deassign")
    public ResponseEntity<Void> deassignUser(
            @PathVariable Long childUserId,
            @AuthenticationPrincipal
            UserDetailsImplementation userDetails
    ) {

        userService.deassignUser(
                userDetails.getId(),
                childUserId,
                userDetails.getId()
        );

        return ResponseEntity.ok().build();
    }
}