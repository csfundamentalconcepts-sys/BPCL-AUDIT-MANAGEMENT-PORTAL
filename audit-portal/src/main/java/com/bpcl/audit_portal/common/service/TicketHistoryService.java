package com.bpcl.audit_portal.common.service;

import com.bpcl.audit_portal.common.dto.TicketHistoryResponse;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.bpcl.audit_portal.common.mapper.TicketHistoryMapper;
import com.bpcl.audit_portal.common.model.Ticket;
import com.bpcl.audit_portal.common.model.TicketHistory;
import com.bpcl.audit_portal.common.repository.TicketHistoryRepository;
import com.bpcl.audit_portal.common.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketHistoryService {

    private final TicketHistoryRepository historyRepository;
    private final TicketRepository ticketRepository;

    public TicketHistoryService(
            TicketHistoryRepository historyRepository,
            TicketRepository ticketRepository
    ) {
        this.historyRepository = historyRepository;
        this.ticketRepository = ticketRepository;
    }

    public List<TicketHistoryResponse> getTicketHistory(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BAMPException(Errors.TICKET_NOT_FOUND));

        List<TicketHistory> histories =
                historyRepository.findByTicketIdOrderByUpdatedAtDesc(ticket.getId());

        return histories.stream()
                .map(TicketHistoryMapper::toDto)
                .toList();
    }
}