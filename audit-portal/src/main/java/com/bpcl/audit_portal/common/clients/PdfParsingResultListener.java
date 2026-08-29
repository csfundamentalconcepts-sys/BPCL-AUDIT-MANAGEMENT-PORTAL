package com.bpcl.audit_portal.common.clients;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.bpcl.audit_portal.common.dto.PdfParsingResultMessage;
import com.bpcl.audit_portal.common.service.VaptService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfParsingResultListener {

    private final ObjectMapper objectMapper;
    private final VaptService vaptService;
    private static final Logger log = LoggerFactory.getLogger(PdfParsingResultListener.class);

    public PdfParsingResultListener(ObjectMapper objectMapper, VaptService vaptService) {
        this.objectMapper = objectMapper;
        this.vaptService= vaptService;
    }
    public void processMessage(ServiceBusReceivedMessageContext context) {
        try {

            String payload = context.getMessage()
                            .getBody()
                            .toString();

            PdfParsingResultMessage message =
                    objectMapper.readValue(
                            payload,
                            PdfParsingResultMessage.class);

            vaptService.saveVulnerabilities(
                    message.getPhaseId(),
                    message.getParsed(),
                    message.getUserId()
            );

        } catch (Exception ex) {
            log.error("Error occurred while listening to parsed result",ex);
        }
    }
    public void processError(ServiceBusErrorContext context) {

        log.error("Service Bus Error", context.getException());
    }
}
