package com.bpcl.audit_portal.common.clients;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.CreateQueueOptions;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void createQueuesIfMissing() {

        ServiceBusAdministrationClient adminClient =
                new ServiceBusAdministrationClientBuilder()
                        .connectionString(connectionString)
                        .buildClient();

        createQueue(adminClient, queueName);
        createQueue(adminClient, resultQueueName);
    }

    private void createQueue(
            ServiceBusAdministrationClient adminClient,
            String queueName) {

        if (!adminClient.getQueueExists(queueName)) {

            adminClient.createQueue(
                    queueName,
                    new CreateQueueOptions()
                            .setDeadLetteringOnMessageExpiration(true)
            );

            System.out.println("Created queue: " + queueName);

        } else {

            System.out.println("Queue already exists: " + queueName);
        }
    }

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