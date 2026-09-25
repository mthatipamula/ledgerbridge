package com.ledgerbridge.settlement.service;

import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.messaging.SettlementEventPublisher;
import com.ledgerbridge.settlement.repository.MoneyMovementTransactionRepository;
import org.springframework.stereotype.Service;

import com.ledgerbridge.settlement.event.SettlementRequestedEvent;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MoneyMovementService {

    private final MoneyMovementTransactionRepository transactionRepository;
    private final SettlementEventPublisher settlementEventPublisher;
    
    public MoneyMovementService(
                MoneyMovementTransactionRepository transactionRepository,
                SettlementEventPublisher settlementEventPublisher) {

        this.transactionRepository = transactionRepository;
        this.settlementEventPublisher = settlementEventPublisher;
    }

    public MoneyMovementTransaction createTransaction(
            String idempotencyKey,
            String sourceAccountId,
            String destinationAccountId,
            BigDecimal amount,
            String currency) {

        return transactionRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> createNewTransaction(
                        idempotencyKey,
                        sourceAccountId,
                        destinationAccountId,
                        amount,
                        currency));
    }

    private MoneyMovementTransaction createNewTransaction(
            String idempotencyKey,
            String sourceAccountId,
            String destinationAccountId,
            BigDecimal amount,
            String currency) {

        MoneyMovementTransaction transaction =
                new MoneyMovementTransaction(
                        UUID.randomUUID(),
                        idempotencyKey,
                        sourceAccountId,
                        destinationAccountId,
                        amount,
                        currency);

        transaction.markValidated();
        transaction.markPending();

        transactionRepository.save(transaction);

        try {
                SettlementRequestedEvent event = new SettlementRequestedEvent(
                        transaction.getId(),
                        sourceAccountId,
                        destinationAccountId,
                        amount,
                        currency);

                settlementEventPublisher.publish(event);

                return transaction;

        } catch (Exception exception) {

            transaction.markFailed();
            transactionRepository.save(transaction);

            throw new IllegalStateException(
                    "Unable to publish settlement event",
                    exception);
        }
    }

    public MoneyMovementTransaction getTransaction(UUID transactionId) {
        return transactionRepository
                .findById(transactionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Money movement transaction not found: " + transactionId));
    }
}