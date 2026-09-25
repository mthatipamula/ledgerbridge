package com.ledgerbridge.settlement.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.event.SettlementRequestedEvent;
import com.ledgerbridge.settlement.blockchain.SettlementLedgerService;
import com.ledgerbridge.settlement.repository.MoneyMovementTransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

@Component
public class SettlementEventWorker {

    private static final String QUEUE_URL =
            "http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/settlement-requests";

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final SettlementLedgerService settlementLedgerService;
    private final MoneyMovementTransactionRepository transactionRepository;

    public SettlementEventWorker(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            SettlementLedgerService settlementLedgerService,
            MoneyMovementTransactionRepository transactionRepository) {

        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.settlementLedgerService = settlementLedgerService;
        this.transactionRepository = transactionRepository;
    }

    @Scheduled(fixedDelay = 2000)
    public void processMessages() {

        ReceiveMessageRequest request =
                ReceiveMessageRequest.builder()
                        .queueUrl(QUEUE_URL)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(1)
                        .build();

        List<Message> messages =
                sqsClient.receiveMessage(request).messages();

        for (Message message : messages) {
            processMessage(message);
        }
    }

    private void processMessage(Message message) {

        try {
            SettlementRequestedEvent event =
                    objectMapper.readValue(
                            message.body(),
                            SettlementRequestedEvent.class);

            TransactionReceipt receipt =
                    settlementLedgerService.recordSettlement(
                            event.transactionId().toString(),
                            event.sourceAccountId(),
                            event.destinationAccountId(),
                            event.amount().movePointRight(2).toBigIntegerExact(),
                            event.currency());

            MoneyMovementTransaction transaction =
                    transactionRepository
                            .findById(event.transactionId())
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Transaction not found: "
                                                    + event.transactionId()));

            transaction.markConfirmed(receipt.getTransactionHash());
            transactionRepository.save(transaction);

            deleteMessage(message);

        } catch (Exception exception) {
            // Leave the message in SQS so it can be retried.
            System.err.println(
                    "Settlement processing failed: "
                            + exception.getMessage());
        }
    }

    private void deleteMessage(Message message) {

        sqsClient.deleteMessage(
                DeleteMessageRequest.builder()
                        .queueUrl(QUEUE_URL)
                        .receiptHandle(message.receiptHandle())
                        .build());
    }
}