package com.bpcl.audit_portal.auth.controller;

import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.auth.service.AuthService;
import com.bpcl.audit_portal.common.dto.SignUpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spoc")
public class SpocController {

    private final AuthService authService;

    public SpocController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasRole('SPOC')")
    public ResponseEntity<String> createUser(
            @RequestBody SignUpRequest request,
            @AuthenticationPrincipal UserDetailsImplementation userDetails
    ){
        authService.registerUser(request, userDetails);
        return ResponseEntity.ok("User created successfully");
    }
}