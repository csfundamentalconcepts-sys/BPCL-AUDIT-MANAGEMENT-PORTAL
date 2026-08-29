package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.model.VaptAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VaptAuditRepository extends JpaRepository<VaptAudit, Long> {

    boolean existsByVaptCardId(Long cardId);

    List<VaptAudit> findByVaptCardId(Long cardId);

    @Query(value = """
            SELECT a.name
            FROM vapt_audits va
            INNER JOIN vapt_cards vc
                ON va.vapt_card_id = vc.id
            INNER JOIN applications a
                ON vc.application_id = a.id
            WHERE va.id = :auditId
            """, nativeQuery = true)
            Optional<String> findApplicationNameByAuditId(
            @Param("auditId") Long auditId
    );
}