package com.ledgerbridge.settlement.repository;

import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMoneyMovementTransactionRepository
        implements MoneyMovementTransactionRepository {

    private final Map<UUID, MoneyMovementTransaction> transactions =
            new ConcurrentHashMap<>();

    private final Map<String, UUID> idempotencyIndex =
            new ConcurrentHashMap<>();

    @Override
    public MoneyMovementTransaction save(MoneyMovementTransaction transaction) {
        transactions.put(transaction.getId(), transaction);
        idempotencyIndex.put(transaction.getIdempotencyKey(), transaction.getId());

        return transaction;
    }

    @Override
    public Optional<MoneyMovementTransaction> findById(UUID id) {
        return Optional.ofNullable(transactions.get(id));
    }

    @Override
    public Optional<MoneyMovementTransaction> findByIdempotencyKey(
            String idempotencyKey) {

        UUID transactionId = idempotencyIndex.get(idempotencyKey);

        if (transactionId == null) {
            return Optional.empty();
        }

        return findById(transactionId);
    }
}