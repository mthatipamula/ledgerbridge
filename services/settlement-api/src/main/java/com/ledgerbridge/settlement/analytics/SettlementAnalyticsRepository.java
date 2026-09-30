package com.ledgerbridge.settlement.analytics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.sql.Timestamp;
import java.util.List;

@Repository
@ConditionalOnProperty(
        name = "ledgerbridge.repository.type",
        havingValue = "postgres")
public class SettlementAnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public SettlementAnalyticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SettlementAnalyticsSummary getSummary() {
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM money_movement_transactions",
                Long.class);

        long totalTransactions = result == null ? 0L : result;

        Map<String, Long> countsByStatus = new LinkedHashMap<>();

        Map<String, BigDecimal> amountsByCurrency = new LinkedHashMap<>();

        jdbcTemplate.query("""
                SELECT status, COUNT(*) AS transaction_count
                FROM money_movement_transactions
                GROUP BY status
                ORDER BY status
                """, (RowCallbackHandler) rs -> countsByStatus.put(
                rs.getString("status"),
                rs.getLong("transaction_count")));

        jdbcTemplate.query("""
                SELECT currency, SUM(amount) AS total_amount
                FROM money_movement_transactions
                GROUP BY currency
                ORDER BY currency
                """, (RowCallbackHandler) rs -> amountsByCurrency.put(
                rs.getString("currency"),
                rs.getBigDecimal("total_amount")));

        return new SettlementAnalyticsSummary(
                totalTransactions,
                Map.copyOf(countsByStatus),
                Map.copyOf(amountsByCurrency),
                countsByStatus.getOrDefault("FAILED", 0L),
                countsByStatus.getOrDefault("DISPUTED", 0L));
    }

    public List<SettlementTransactionExportRow> findAllForExport() {
    return jdbcTemplate.query("""
            SELECT id,
                   idempotency_key,
                   source_account_id,
                   destination_account_id,
                   amount,
                   currency,
                   status,
                   blockchain_tx_hash,
                   created_at,
                   updated_at
            FROM money_movement_transactions
            ORDER BY created_at, id
            """,
            (rs, rowNum) -> new SettlementTransactionExportRow(
                    rs.getString("id"),
                    rs.getString("idempotency_key"),
                    rs.getString("source_account_id"),
                    rs.getString("destination_account_id"),
                    rs.getBigDecimal("amount"),
                    rs.getString("currency"),
                    rs.getString("status"),
                    rs.getString("blockchain_tx_hash"),
                    rs.getTimestamp("created_at").toInstant(),
                    rs.getTimestamp("updated_at").toInstant()
            ));
    }

        public record SettlementTransactionExportRow(
                String id,
                String idempotencyKey,
                String sourceAccountId,
                String destinationAccountId,
                BigDecimal amount,
                String currency,
                String status,
                String blockchainTxHash,
                java.time.Instant createdAt,
                java.time.Instant updatedAt) {
       }

    public record SettlementAnalyticsSummary(
            long totalTransactions,
            Map<String, Long> countsByStatus,
            Map<String, BigDecimal> amountsByCurrency,
            long failedTransactions,
            long disputedTransactions) {
    }
}
