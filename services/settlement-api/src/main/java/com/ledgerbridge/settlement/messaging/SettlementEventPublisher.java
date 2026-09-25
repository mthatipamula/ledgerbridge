package com.ledgerbridge.settlement.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerbridge.settlement.event.SettlementRequestedEvent;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class SettlementEventPublisher {

    private static final String QUEUE_URL =
            "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/settlement-requests";

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    public SettlementEventPublisher(
            SqsClient sqsClient,
            ObjectMapper objectMapper) {

        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
    }

    public void publish(SettlementRequestedEvent event) {

        try {
            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest request =
                    SendMessageRequest.builder()
                            .queueUrl(QUEUE_URL)
                            .messageBody(messageBody)
                            .build();

            sqsClient.sendMessage(request);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to serialize settlement event",
                    exception);
        }
    }
}