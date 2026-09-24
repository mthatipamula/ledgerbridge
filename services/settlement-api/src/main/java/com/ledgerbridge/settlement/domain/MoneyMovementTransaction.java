package com.ledgerbridge.settlement.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class MoneyMovementTransaction {

    private final UUID id;
    private final String idempotencyKey;
    private final String sourceAccountId;
    private final String destinationAccountId;
    private final BigDecimal amount;
    private final String currency;

    private TransactionStatus status;
    private String blockchainTransactionHash;
    private final Instant createdAt;
    private Instant updatedAt;

    public MoneyMovementTransaction(
            UUID id,
            String idempotencyKey,
            String sourceAccountId,
            String destinationAccountId,
            BigDecimal amount,
            String currency) {

        this.id = Objects.requireNonNull(id, "id must not be null");
        this.idempotencyKey = requireText(idempotencyKey, "idempotencyKey");
        this.sourceAccountId = requireText(sourceAccountId, "sourceAccountId");
        this.destinationAccountId = requireText(destinationAccountId, "destinationAccountId");
        this.amount = requirePositiveAmount(amount);
        this.currency = requireText(currency, "currency").toUpperCase();

        this.status = TransactionStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getBlockchainTransactionHash() {
        return blockchainTransactionHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void markValidated() {
        transitionTo(TransactionStatus.VALIDATED);
    }

    public void markPending() {
        transitionTo(TransactionStatus.PENDING);
    }

    public void markProcessing() {
        transitionTo(TransactionStatus.PROCESSING);
    }

    public void markConfirmed(String blockchainTransactionHash) {
        this.blockchainTransactionHash =
                requireText(blockchainTransactionHash, "blockchainTransactionHash");

        transitionTo(TransactionStatus.CONFIRMED);
    }

    public void markFailed() {
        transitionTo(TransactionStatus.FAILED);
    }

    public void markDisputed() {
        transitionTo(TransactionStatus.DISPUTED);
    }

    private void transitionTo(TransactionStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "newStatus must not be null");
        this.updatedAt = Instant.now();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }

        return value;
    }

    private static BigDecimal requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }

        return amount;
    }

    public enum TransactionStatus {
        CREATED,
        VALIDATED,
        PENDING,
        PROCESSING,
        CONFIRMED,
        FAILED,
        DISPUTED
    }
}