package com.ledgerbridge.settlement.service;

import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.repository.MoneyMovementTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MoneyMovementService {

    private final MoneyMovementTransactionRepository transactionRepository;

    public MoneyMovementService(MoneyMovementTransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
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

        return transactionRepository.save(transaction);
    }
}