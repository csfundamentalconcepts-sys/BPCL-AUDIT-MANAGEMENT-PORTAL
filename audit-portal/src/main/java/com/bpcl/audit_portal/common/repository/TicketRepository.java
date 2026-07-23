package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.constants.TicketStatus;
import com.bpcl.audit_portal.common.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("""
        SELECT t FROM Ticket t
        WHERE t.application.id = :applicationId
        ORDER BY
        CASE t.status
            WHEN 'IN_PROGRESS' THEN 1
            WHEN 'HOLD' THEN 2
            WHEN 'NOT_STARTED' THEN 3
            WHEN 'CLOSED' THEN 4
        END,
        t.updatedAt DESC
    """)

    List<Ticket> findByApplicationIdOrdered(Long applicationId);

    Long countByStatus(TicketStatus status);

    Long countByApplicationId(Long applicationId);

    Long countByApplicationIdAndStatus(Long applicationId, TicketStatus status);

}