package com.bpcl.audit_portal.auth.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.auth.service.AuthService;
import com.bpcl.audit_portal.common.dto.SignUpRequest;
import com.bpcl.audit_portal.common.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;

    public AdminController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createHead(
            @RequestBody SignUpRequest request,
            @AuthenticationPrincipal UserDetailsImplementation userDetails
    ) {
        UserDto response = authService.registerUser(request, userDetails);
        return ResponseEntity.ok(response);
    }
}
