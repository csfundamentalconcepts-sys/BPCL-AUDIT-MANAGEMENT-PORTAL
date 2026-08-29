package com.bpcl.audit_portal.common.clients;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceBusConfig {

    @Value("${azure.servicebus.connection-string}")
    private String connectionString;

    @Value("${azure.servicebus.queue-name}")
    private String queueName;

    @Value("${azure.servicebus.result-queue-name}")
    private String resultQueueName;

    @Bean
    public ServiceBusSenderClient serviceBusSenderClient() {

        return new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .sender()
                .queueName(queueName)
                .buildClient();
    }

    @Bean
    public ServiceBusProcessorClient resultQueueProcessor(
            PdfParsingResultListener listener) {

        ServiceBusProcessorClient processor =
                new ServiceBusClientBuilder()
                        .connectionString(connectionString)
                        .processor()
                        .queueName(resultQueueName)
                        .processMessage(listener::processMessage)
                        .processError(listener::processError)
                        .buildProcessorClient();

        processor.start();

        return processor;
    }
}
