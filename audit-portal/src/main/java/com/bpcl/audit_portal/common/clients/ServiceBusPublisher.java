package com.bpcl.audit_portal.common.clients;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.bpcl.audit_portal.common.dto.PdfParsingMessage;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceBusPublisher {

    private final ServiceBusSenderClient senderClient;
    private final ObjectMapper objectMapper;

    public ServiceBusPublisher(ServiceBusSenderClient senderClient, ObjectMapper objectMapper) {
        this.senderClient = senderClient;
        this.objectMapper = objectMapper;
    }

    public void publish(PdfParsingMessage message) {

        try {

            String payload =
                    objectMapper.writeValueAsString(message);

            senderClient.sendMessage(
                    new ServiceBusMessage(payload)
            );

        } catch (JsonProcessingException ex) {
            throw  new BAMPException(Errors.INTERNAL_ISSUE);
        }
    }
}
