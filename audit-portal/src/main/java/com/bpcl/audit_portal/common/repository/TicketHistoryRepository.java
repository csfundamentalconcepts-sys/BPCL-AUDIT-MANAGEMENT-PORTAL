package com.bpcl.audit_portal.common.repository;

import com.bpcl.audit_portal.common.model.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long> {

    List<TicketHistory> findByTicketIdOrderByUpdatedAtDesc(Long ticketId);
}
