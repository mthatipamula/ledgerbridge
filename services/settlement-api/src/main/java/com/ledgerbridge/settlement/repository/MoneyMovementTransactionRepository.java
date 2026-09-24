package com.ledgerbridge.settlement.repository;

import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;

import java.util.Optional;
import java.util.UUID;

public interface MoneyMovementTransactionRepository {

    MoneyMovementTransaction save(MoneyMovementTransaction transaction);

    Optional<MoneyMovementTransaction> findById(UUID id);

    Optional<MoneyMovementTransaction> findByIdempotencyKey(String idempotencyKey);
}