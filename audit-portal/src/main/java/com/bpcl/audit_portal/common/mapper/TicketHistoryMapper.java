package com.bpcl.audit_portal.common.mapper;

import com.bpcl.audit_portal.common.dto.TicketHistoryResponse;
import com.bpcl.audit_portal.common.model.TicketHistory;
import org.springframework.stereotype.Component;

@Component
public class TicketHistoryMapper {

    private TicketHistoryMapper() {
    }

    public static TicketHistoryResponse toDto(TicketHistory history) {
        return TicketHistoryResponse.builder()
                .fieldName(history.getFieldName())
                .oldValue(history.getOldValue())
                .newValue(history.getNewValue())
                .updatedBy(history.getUpdatedBy().getUserName())
                .updatedAt(history.getUpdatedAt())
                .build();
    }
}
