package com.ledgerbridge.settlement.repository;

import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.domain.MoneyMovementTransaction.TransactionStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        name = "ledgerbridge.repository.type",
        havingValue = "postgres")
public class PostgresMoneyMovementTransactionRepository
        implements MoneyMovementTransactionRepository {

    private final JdbcTemplate jdbcTemplate;

    public PostgresMoneyMovementTransactionRepository(
            JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public MoneyMovementTransaction save(
            MoneyMovementTransaction transaction) {

        String sql = """
                INSERT INTO money_movement_transactions (
                    id,
                    idempotency_key,
                    source_account_id,
                    destination_account_id,
                    amount,
                    currency,
                    status,
                    blockchain_tx_hash,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id)
                DO UPDATE SET
                    status = EXCLUDED.status,
                    blockchain_tx_hash = EXCLUDED.blockchain_tx_hash,
                    updated_at = EXCLUDED.updated_at
                """;

        jdbcTemplate.update(
                sql,
                transaction.getId(),
                transaction.getIdempotencyKey(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus().name(),
                transaction.getBlockchainTransactionHash(),
                Timestamp.from(transaction.getCreatedAt()),
                Timestamp.from(transaction.getUpdatedAt())
        );

        return transaction;
    }

    @Override
    public Optional<MoneyMovementTransaction> findById(UUID id) {

        String sql = """
                SELECT *
                FROM money_movement_transactions
                WHERE id = ?
                """;

        return jdbcTemplate.query(
                sql,
                this::mapRow,
                id
        ).stream().findFirst();
    }

    @Override
    public Optional<MoneyMovementTransaction> findByIdempotencyKey(
            String idempotencyKey) {

        String sql = """
                SELECT *
                FROM money_movement_transactions
                WHERE idempotency_key = ?
                """;

        return jdbcTemplate.query(
                sql,
                this::mapRow,
                idempotencyKey
        ).stream().findFirst();
    }

    private MoneyMovementTransaction mapRow(
            java.sql.ResultSet rs,
            int rowNum) throws java.sql.SQLException {

        MoneyMovementTransaction transaction =
                new MoneyMovementTransaction(
                        rs.getObject("id", UUID.class),
                        rs.getString("idempotency_key"),
                        rs.getString("source_account_id"),
                        rs.getString("destination_account_id"),
                        rs.getBigDecimal("amount"),
                        rs.getString("currency")
                );

        TransactionStatus status =
                TransactionStatus.valueOf(
                        rs.getString("status"));

        switch (status) {
            case CREATED -> {
                // Constructor already creates CREATED state.
            }
            case VALIDATED -> transaction.markValidated();
            case PENDING -> transaction.markPending();
            case PROCESSING -> transaction.markProcessing();
            case CONFIRMED -> transaction.markConfirmed(
                    rs.getString("blockchain_tx_hash"));
            case FAILED -> transaction.markFailed();
            case DISPUTED -> transaction.markDisputed();
        }

        return transaction;
    }
}