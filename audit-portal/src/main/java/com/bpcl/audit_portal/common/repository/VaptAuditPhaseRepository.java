package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.model.VaptAuditPhase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface VaptAuditPhaseRepository extends JpaRepository<VaptAuditPhase, Long> {
    Optional<VaptAuditPhase> findByVaptAuditIdAndPhaseNumber(Long auditId, Integer phaseNumber);
    List<VaptAuditPhase> findByVaptAuditId(Long auditId);
    Optional<VaptAuditPhase> findTopByVaptAudit_IdOrderByPhaseNumberDesc(Long auditId);
}