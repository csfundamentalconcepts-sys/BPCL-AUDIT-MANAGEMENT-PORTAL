package com.bpcl.audit_portal.auth.controller;

import com.bpcl.audit_portal.auth.jwt.JwtUtils;
import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.auth.service.AuthService;
import com.bpcl.audit_portal.auth.service.RefreshTokenService;
import com.bpcl.audit_portal.common.dto.AuthResponse;
import com.bpcl.audit_portal.common.dto.LoginRequest;
import com.bpcl.audit_portal.common.dto.UserDto;
import com.bpcl.audit_portal.common.mapper.UserDtoMapper;
import com.bpcl.audit_portal.common.model.*;
import com.bpcl.audit_portal.common.repository.RoleRepository;
import com.bpcl.audit_portal.common.repository.UserRepository;
import com.bpcl.audit_portal.common.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final UserDtoMapper userDtoMapper;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    public  AuthController(JwtUtils jwtUtils, UserRepository userRepository, UserService userService, UserDtoMapper userDtoMapper, RefreshTokenService refreshTokenService, RoleRepository roleRepository,  PasswordEncoder passwordEncoder, AuthService authService){
        this.jwtUtils=jwtUtils;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.userDtoMapper = userDtoMapper;
        this.refreshTokenService = refreshTokenService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        AuthResponse authResponse = authService.authenticate(loginRequest,request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken().getToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse.getLoginResponse());
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<?>refreshToken(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();
        String requestRefreshToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    requestRefreshToken = cookie.getValue();
                    break;
                }
            }
        }
        if (requestRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please Login again!");
        }
        AuthResponse authResponse = authService.refreshToken(requestRefreshToken);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", authResponse.getRefreshToken().getToken())
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(authResponse.getLoginResponse());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("Password reset link has been sent.");
    }

    @PostMapping("/password-reset")
    public ResponseEntity<?> passwordReset(@RequestParam String token , @RequestParam String newPassword){
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset is  successful.");

    }

    @PostMapping("/logout")
    public ResponseEntity<?> logOutUser(HttpServletRequest request) {
        String requestRefreshToken = null;
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    requestRefreshToken = cookie.getValue();
                    break;
                }
            }
        }
        authService.logout(requestRefreshToken);
        SecurityContextHolder.clearContext();
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body("Logged out successfully");
    }

    @GetMapping("/user")
    public ResponseEntity<?> userDetails(
            @AuthenticationPrincipal UserDetailsImplementation userDetails) {

        return ResponseEntity.ok(
                userService.getCurrentUser(userDetails.getId())
        );
    }
}

