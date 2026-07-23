package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.constants.TicketStatus;
import com.bpcl.audit_portal.common.model.TicketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface TicketAssignmentRepository extends JpaRepository<TicketAssignment, Long> {

    Optional<TicketAssignment> findByTicketIdAndActiveTrue(Long ticketId);

    List<TicketAssignment> findByAssignedToIdAndActiveTrue(Long userId);

    boolean existsByTicketIdAndAssignedToIdAndActiveTrue(Long ticketId, Long userId);

    void deleteByTicketIdAndActiveTrue(Long ticketId);

    List<TicketAssignment> findByTicketIdOrderByAssignedAtDesc(Long ticketId);

    @Query("""
        SELECT COUNT(ta)
        FROM TicketAssignment ta
        WHERE ta.assignedTo.id = :userId
          AND ta.active = true
    """)
    long countAssignedTickets(Long userId);

    @Query("""
        SELECT COUNT(ta)
        FROM TicketAssignment ta
        WHERE ta.assignedTo.id = :userId
          AND ta.active = true
          AND ta.ticket.status = :status
    """)
    long countAssignedTicketsByStatus(Long userId, TicketStatus status);
}