package com.bpcl.audit_portal.auth.service;

import com.bpcl.audit_portal.auth.jwt.JwtUtils;
import com.bpcl.audit_portal.auth.model.PasswordResetToken;
import com.bpcl.audit_portal.auth.model.RefreshToken;
import com.bpcl.audit_portal.auth.model.UserDetailsImplementation;
import com.bpcl.audit_portal.auth.repository.PasswordResetTokenRepository;
import com.bpcl.audit_portal.auth.repository.RefreshTokenRepository;
import com.bpcl.audit_portal.common.constants.AppRole;
import com.bpcl.audit_portal.common.dto.*;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.UserDtoMapper;
import com.bpcl.audit_portal.common.model.*;
import com.bpcl.audit_portal.common.repository.*;

import com.bpcl.audit_portal.common.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.bpcl.audit_portal.auth.service.Util.validateRoleCreation;
import static com.bpcl.audit_portal.common.constants.AppRole.ADMIN;


@Service
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationRepository applicationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserDtoMapper userDtoMapper;
    private final UserService userService;
    private final UserAssignmentRepository userAssignmentRepository;
    private final ApplicationAssignmentRepository
            applicationAssignmentRepository;


    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    public AuthService(
            RefreshTokenService refreshTokenService,
            UserRepository userRepository,
            UserService userService,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtils jwtUtils,
            RefreshTokenRepository refreshTokenRepository,
            ApplicationRepository applicationRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserDtoMapper userDtoMapper,
            UserAssignmentRepository userAssignmentRepository,
            ApplicationAssignmentRepository applicationAssignmentRepository
    ) {
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenRepository = refreshTokenRepository;
        this.applicationRepository = applicationRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userDtoMapper = userDtoMapper;
        this.userService = userService;
        this.userAssignmentRepository = userAssignmentRepository;
        this.applicationAssignmentRepository =
                applicationAssignmentRepository;
    }

    @Transactional
    public UserDto registerUser(
            SignUpRequest signUpRequest,
            UserDetailsImplementation userDetails
    ) {

        AppRole creatorRole = userDetails.getAuthorities()
                .stream()
                .filter(a -> a.getAuthority().startsWith("ROLE_"))
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .map(AppRole::valueOf)
                .findFirst()
                .orElseThrow(() ->
                        new BAMPException(Errors.UNAUTHORIZED));

        validateRoleCreation(
                creatorRole,
                signUpRequest.getRole()
        );

        if (userRepository.existsByUserName(
                signUpRequest.getUserName()
        )) {

            throw new BAMPException(
                    Errors.USERNAME_ALREADY_IN_USE
            );
        }

        Role role = roleRepository
                .findByRoleName(signUpRequest.getRole())
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.ROLE_NOT_FOUND
                        ));

        User creator = userRepository.findById(
                        userDetails.getId()
                )
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        User user = User.builder()
                .userName(signUpRequest.getUserName())
                .fullName(signUpRequest.getFullName())
                .password(
                        passwordEncoder.encode(
                                signUpRequest.getPassword()
                        )
                )
                .role(role)
                .createdBy(creator)
                .logout(true)
                .isActive(true)
                .build();

        userRepository.save(user);

        UserAssignment assignment =
                UserAssignment.builder()
                        .parentUser(creator)
                        .childUser(user)
                        .assignedBy(creator)
                        .active(true)
                        .build();

        userAssignmentRepository.save(assignment);

        return userDtoMapper.toDto(user);
    }

    @Transactional
    public AuthResponse authenticate(
            LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getUserName(),
                                loginRequest.getPassword()
                        )
                );
        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        UserDetailsImplementation userDetails =
                (UserDetailsImplementation)
                        authentication.getPrincipal();

        User user = userRepository.findById(
                        userDetails.getId()
                )
                .orElseThrow(() ->
                        new BAMPException(Errors.USER_NOT_FOUND));

        user.setLogout(false);
        userRepository.save(user);

        List<Long> applicationIds =
                applicationAssignmentRepository
                        .findByAssignedToIdAndActiveTrue(
                                user.getId()
                        )
                        .stream()
                        .map(ApplicationAssignment::getApplication)
                        .map(Application::getId)
                        .distinct()
                        .toList();

        String jwtToken = jwtUtils.generateJwtToken(
                user.getId(),
                user.getUserName(),
                user.getRole().getRoleName().name()
        );

        LoginResponse response = new LoginResponse(
                user.getUserName(),
                user.getRole().getRoleName().name(),
                jwtToken
        );

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return new AuthResponse(response, refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(String token) {

        RefreshToken refreshToken =
                refreshTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new BAMPException(
                                        Errors.REFRESH_TOKEN_NOT_FOUND
                                ));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BAMPException(Errors.UNAUTHORIZED);
        }

        List<Long> applicationIds =
                applicationAssignmentRepository
                        .findByAssignedToIdAndActiveTrue(
                                user.getId()
                        )
                        .stream()
                        .map(ApplicationAssignment::getApplication)
                        .map(Application::getId)
                        .distinct()
                        .toList();

        String jwtToken = jwtUtils.generateJwtToken(
                user.getId(),
                user.getUserName(),
                user.getRole().getRoleName().name()
        );

        String role = user.getRole()
                .getRoleName()
                .name();

        LoginResponse response = new LoginResponse(
                user.getUserName(),
                role,
                jwtToken
        );

        int updated = refreshTokenRepository
                .markRevokedIfNotAlready(
                        refreshToken.getToken()
                );

        if (updated == 0) {

            RefreshToken newRefreshToken =
                    refreshTokenRepository
                            .findByToken(
                                    refreshToken.getNewToken()
                            )
                            .orElseThrow(() ->
                                    new BAMPException(
                                            Errors
                                                    .REFRESH_TOKEN_NOT_FOUND
                                    ));

            return new AuthResponse(
                    response,
                    newRefreshToken
            );
        }

        RefreshToken newRefreshToken =
                refreshTokenService.rotateRefreshToken(
                        refreshToken
                );

        return new AuthResponse(
                response,
                newRefreshToken
        );
    }

    public String generatePasswordResetToken(
            String username
    ) {

        User user = userRepository.findByUserName(
                        username
                )
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        String token = UUID.randomUUID().toString();

        Instant expiryDate = Instant.now()
                .plus(10, ChronoUnit.MINUTES);

        PasswordResetToken resetToken =
                new PasswordResetToken(
                        token,
                        expiryDate,
                        user
                );

        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    @Transactional
    public void resetPassword(
            String token,
            String newPassword
    ) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository.findByToken(token)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Password reset failed: invalid token. token={}",
                                    token
                            );

                            return new BAMPException(
                                    Errors.INVALID_PASSWORD_RESET_TOKEN
                            );
                        });

        if (resetToken.isUsed()) {

            log.warn(
                    "Password reset failed: token already used. token={}, userId={}",
                    token,
                    resetToken.getUser().getId()
            );

            throw new BAMPException(
                    Errors.PASSWORD_RESET_TOKEN_ALREADY_USED
            );
        }

        if (resetToken.getExpiryDate()
                .isBefore(Instant.now())) {

            log.warn(
                    "Password reset failed: token expired. token={}, userId={}, expiryDate={}",
                    token,
                    resetToken.getUser().getId(),
                    resetToken.getExpiryDate()
            );

            throw new BAMPException(
                    Errors.PASSWORD_RESET_TOKEN_EXPIRED
            );
        }

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(newPassword)
        );

        userRepository.save(user);

        resetToken.setUsed(true);

        passwordResetTokenRepository.save(resetToken);
    }

    public void forgotPassword(String email) {

        userRepository.findByUserName(email)
                .orElseThrow(() ->
                        new BAMPException(
                                Errors.USER_NOT_FOUND
                        ));

        String token =
                generatePasswordResetToken(email);
        // email integration pending
    }

    @Transactional
    public void logout(String refreshToken) {

        if (refreshToken != null) {
            refreshTokenService.revokeToken(refreshToken);
        }

        RefreshToken token = refreshTokenRepository
                .findByToken(refreshToken)
                .orElse(null);

        if (token != null) {

            User user = token.getUser();

            user.setLogout(true);

            userRepository.save(user);
        }
    }
}