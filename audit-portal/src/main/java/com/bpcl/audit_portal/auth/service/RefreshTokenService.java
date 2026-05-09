package com.bpcl.audit_portal.auth.service;

import com.bpcl.audit_portal.auth.model.RefreshToken;
import com.bpcl.audit_portal.auth.repository.RefreshTokenRepository;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.model.User;
import com.bpcl.audit_portal.common.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
public class RefreshTokenService {

    @Value("${spring.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,UserRepository userRepository){
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    // CREATE TOKEN
    public RefreshToken createRefreshToken(User user){
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .token(generateSecureToken())
                .newToken(null)
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    // VERIFY TOKEN
    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            log.warn("Refresh token expired. token={}, userId={}, expiryDate={}",
                    token.getToken(),
                    token.getUser().getId(),
                    token.getExpiryDate());
            throw new BAMPException(Errors.PASSWORD_RESET_TOKEN_EXPIRED);
        }
    }

    // ROTATE TOKEN
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken){
        RefreshToken newToken = RefreshToken.builder()
                .user(oldToken.getUser())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .token(generateSecureToken())
                .newToken(null)
                .revoked(false)
                .build();
        refreshTokenRepository.save(newToken);
        oldToken.setNewToken(newToken.getToken());
        refreshTokenRepository.save(oldToken);
        return newToken;
    }

    //DELETE USER TOKENS
    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BAMPException(Errors.USER_NOT_FOUND));
        user.setIsActive(false);
        userRepository.save(user);
    }

    //SECURE TOKEN GENERATOR
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    // SETTING REVOKE FLAG FOR LOGOUT
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BAMPException(Errors.REFRESH_TOKEN_NOT_FOUND));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}




