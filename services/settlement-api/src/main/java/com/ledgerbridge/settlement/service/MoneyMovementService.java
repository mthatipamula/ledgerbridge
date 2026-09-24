package com.ledgerbridge.settlement.service;

import com.ledgerbridge.settlement.blockchain.SettlementLedgerService;
import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.repository.MoneyMovementTransactionRepository;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

@Service
public class MoneyMovementService {

    private final MoneyMovementTransactionRepository transactionRepository;
    private final SettlementLedgerService settlementLedgerService;

    public MoneyMovementService(
            MoneyMovementTransactionRepository transactionRepository,
            SettlementLedgerService settlementLedgerService) {

        this.transactionRepository = transactionRepository;
        this.settlementLedgerService = settlementLedgerService;
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
            TransactionReceipt receipt =
                    settlementLedgerService.recordSettlement(
                            transaction.getId().toString(),
                            sourceAccountId,
                            destinationAccountId,
                            amount.movePointRight(2).toBigIntegerExact(),
                            currency
                    );

            transaction.markConfirmed(
                    receipt.getTransactionHash());

            return transactionRepository.save(transaction);

        } catch (Exception exception) {

            transaction.markFailed();
            transactionRepository.save(transaction);

            throw new IllegalStateException(
                    "Blockchain settlement failed",
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