
package com.ledgerbridge.settlement.analytics;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.FieldValue;
import com.google.cloud.bigquery.TableResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobInfo;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BigQuerySettlementExportService {

    private final BigQuery bigQuery;
    private final SettlementAnalyticsRepository repository;
    private final BigQueryAnalyticsProperties properties;

    public BigQuerySettlementExportService(
            BigQuery bigQuery,
            SettlementAnalyticsRepository repository,
            BigQueryAnalyticsProperties properties) {
        this.bigQuery = bigQuery;
        this.repository = repository;
        this.properties = properties;
    }

    public ExportResult exportAll() {
        var rows = repository.findAllForExport();

        if (rows.isEmpty()) {
            return new ExportResult(0, "No transactions to export");
        }

        String projectId = properties.projectId();
        String datasetId = properties.datasetId();
        String tableId = properties.tableId();
        String stagingTableId = tableId + "_staging";

        TableId staging = TableId.of(projectId, datasetId, stagingTableId);

        List<Map<String, Object>> records = rows.stream()
                .map(row -> {
                    Map<String, Object> record = new LinkedHashMap<>();
                    record.put("id", row.id());
                    record.put("idempotency_key", row.idempotencyKey());
                    record.put("source_account_id", row.sourceAccountId());
                    record.put("destination_account_id", row.destinationAccountId());
                    record.put("amount", row.amount().toPlainString());
                    record.put("currency", row.currency());
                    record.put("status", row.status());
                    record.put("blockchain_tx_hash", row.blockchainTxHash());
                    record.put("created_at", row.createdAt().toString());
                    record.put("updated_at", row.updatedAt().toString());
                    return record;
                })
                .toList();

        // Clear the staging table so this batch contains only the current snapshot.
        runQuery("""
                TRUNCATE TABLE `%s.%s.%s`
                """.formatted(projectId, datasetId, stagingTableId));

        InsertAllRequest.Builder requestBuilder =
                InsertAllRequest.newBuilder(staging);

        for (Map<String, Object> record : records) {
            requestBuilder.addRow((String) record.get("id"), record);
        }

        InsertAllResponse response =
                bigQuery.insertAll(requestBuilder.build());

        if (response.hasErrors()) {
            throw new IllegalStateException(
                    "BigQuery staging insert errors: " + response.getInsertErrors());
        }

        String mergeSql = """
                MERGE `%s.%s.%s` AS target
                USING (
                    SELECT * EXCEPT(row_num)
                    FROM (
                        SELECT staging.*,
                               ROW_NUMBER() OVER (
                                   PARTITION BY id
                                   ORDER BY updated_at DESC
                               ) AS row_num
                        FROM `%s.%s.%s` AS staging
                    )
                    WHERE row_num = 1
                ) AS source
                ON target.id = source.id
                WHEN MATCHED THEN UPDATE SET
                    idempotency_key = source.idempotency_key,
                    source_account_id = source.source_account_id,
                    destination_account_id = source.destination_account_id,
                    amount = source.amount,
                    currency = source.currency,
                    status = source.status,
                    blockchain_tx_hash = source.blockchain_tx_hash,
                    created_at = source.created_at,
                    updated_at = source.updated_at
                WHEN NOT MATCHED THEN INSERT (
                    id, idempotency_key, source_account_id,
                    destination_account_id, amount, currency, status,
                    blockchain_tx_hash, created_at, updated_at
                ) VALUES (
                    source.id, source.idempotency_key, source.source_account_id,
                    source.destination_account_id, source.amount, source.currency,
                    source.status, source.blockchain_tx_hash,
                    source.created_at, source.updated_at
                )
                """.formatted(
                projectId, datasetId, tableId,
                projectId, datasetId, stagingTableId);

        runQuery(mergeSql);

        return new ExportResult(records.size(), "Export merged into BigQuery");
    }

    private void runQuery(String sql) {
        QueryJobConfiguration configuration =
                QueryJobConfiguration.newBuilder(sql).build();

        try {
            Job job = bigQuery.create(
                    JobInfo.newBuilder(configuration).build());

            Job completedJob = job.waitFor();

            if (completedJob == null) {
                throw new IllegalStateException(
                        "BigQuery job disappeared before completion");
            }

            if (completedJob.getStatus().getError() != null) {
                throw new IllegalStateException(
                        "BigQuery job failed: "
                                + completedJob.getStatus().getError());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while waiting for BigQuery job", exception);
        }
    }


public List<DailySettlementMetric> getDailySettlementMetrics() {
    String sql = """
            SELECT
                settlement_date,
                currency,
                status,
                transaction_count,
                total_amount,
                failed_count,
                disputed_count
            FROM `%s.%s.daily_settlement_metrics`
            ORDER BY settlement_date DESC, currency, status
            """.formatted(
            properties.projectId(),
            properties.datasetId()
    );

    try {
        QueryJobConfiguration configuration =
                QueryJobConfiguration.newBuilder(sql).build();

        TableResult result = bigQuery.query(configuration);

        List<DailySettlementMetric> metrics = new ArrayList<>();

        for (var row : result.iterateAll()) {
            metrics.add(new DailySettlementMetric(
                    LocalDate.parse(
                            row.get("settlement_date").getStringValue()),
                    row.get("currency").getStringValue(),
                    row.get("status").getStringValue(),
                    row.get("transaction_count").getLongValue(),
                    row.get("total_amount").getNumericValue(),
                    row.get("failed_count").getLongValue(),
                    row.get("disputed_count").getLongValue()
            ));
        }

        return List.copyOf(metrics);

    } catch (InterruptedException exception) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(
                "Interrupted while querying daily settlement metrics",
                exception
        );
    }
}

        public record DailySettlementMetric(
                LocalDate settlementDate,
                String currency,
                String status,
                long transactionCount,
                BigDecimal totalAmount,
                long failedCount,
                long disputedCount
        ) {}

    public record ExportResult(int exportedRows, String message) {
    }
}