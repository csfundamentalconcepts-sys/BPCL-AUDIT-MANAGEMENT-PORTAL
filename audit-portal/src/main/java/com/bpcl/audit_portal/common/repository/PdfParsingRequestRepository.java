package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.constants.ParsingStatus;
import com.bpcl.audit_portal.common.model.PdfParsingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfParsingRequestRepository extends JpaRepository<PdfParsingRequest,Long>{

    boolean existsByPhaseIdAndStatus(
            Long auditId,
            ParsingStatus status
    );
}
