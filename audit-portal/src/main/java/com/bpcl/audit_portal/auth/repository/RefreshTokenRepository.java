package com.bpcl.audit_portal.auth.repository;

import com.bpcl.audit_portal.auth.model.RefreshToken;
import com.bpcl.audit_portal.common.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Used during refresh flow to locate the token.
     */
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query(""" 
       UPDATE RefreshToken r
       SET r.revoked = true
       WHERE r.token = :token AND r.revoked = false
       """)
    int markRevokedIfNotAlready(@Param("token") String token);

    /**
     * Optional safety check if you ever rely on revoked flag.
     * Safe to keep for future-proofing.
     */

    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    /**
     * Useful for multi-device session management.
     * Example: logout from all devices.
     */
    List<RefreshToken> findAllByUser(User user);

    /**
     * Logout all devices for a user.
     */
    @Modifying
    @Transactional
    int deleteByUser(User user);

    /**
     * Used in refresh token rotation (VERY IMPORTANT).
     * R1 used → delete R1 → create R2
     */
    @Modifying
    @Transactional
    int deleteByToken(String token);

    /**
     * Used by midnight scheduler to clean expired tokens.
     * Optional but recommended.
     */
    @Modifying
    @Transactional
    int deleteByExpiryDateBefore(Instant now);
}

