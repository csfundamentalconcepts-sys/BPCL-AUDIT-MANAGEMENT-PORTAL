package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.model.TicketAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketAssignmentRepository extends JpaRepository<TicketAssignment, Long> {

    Optional<TicketAssignment> findByTicketIdAndActiveTrue(Long ticketId);

    List<TicketAssignment> findByAssignedToIdAndActiveTrue(Long userId);

    boolean existsByTicketIdAndAssignedToIdAndActiveTrue(Long ticketId, Long userId);

    void deleteByTicketIdAndActiveTrue(Long ticketId);
}