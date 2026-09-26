package com.bpcl.audit_portal.common.clients;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.bpcl.audit_portal.common.dto.PdfParsingMessage;
import com.bpcl.audit_portal.common.exceptions.BAMPException;
import com.bpcl.audit_portal.common.exceptions.Errors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service

public class ServiceBusPublisher {

    private final ServiceBusSenderClient senderClient;

    public ServiceBusPublisher(ServiceBusSenderClient senderClient) {
        this.senderClient = senderClient;
    }

    public void publish(PdfParsingMessage message) {

        try {
            final ObjectMapper mapper = new ObjectMapper();
            String payload =
                    mapper.writeValueAsString(message);

            senderClient.sendMessage(
                    new ServiceBusMessage(payload)
            );

        } catch (JsonProcessingException ex) {
            throw  new BAMPException(Errors.INTERNAL_ISSUE);
        }
    }
}
