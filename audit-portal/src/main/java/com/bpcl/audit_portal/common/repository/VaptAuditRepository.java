package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.model.VaptAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VaptAuditRepository extends JpaRepository<VaptAudit, Long> {

    boolean existsByVaptCardIdAndAuditYear(Long cardId, Integer auditYear);

    Optional<VaptAudit> findByVaptCardIdAndAuditYear(Long cardId, Integer auditYear);

    List<VaptAudit> findByVaptCardIdOrderByAuditYearDesc(Long cardId);
}