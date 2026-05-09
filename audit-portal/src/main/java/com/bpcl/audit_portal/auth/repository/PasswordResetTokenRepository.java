package com.bpcl.audit_portal.auth.repository;

import com.bpcl.audit_portal.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    public Optional<PasswordResetToken> findByToken(String token);
    public void deleteByExpiryDateBefore(Instant now);
}

