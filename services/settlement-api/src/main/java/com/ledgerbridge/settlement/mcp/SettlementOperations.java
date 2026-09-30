package com.ledgerbridge.settlement.mcp;

import org.springframework.ai.tool.annotation.Tool;
import com.ledgerbridge.settlement.blockchain.LedgerSettlement;
import com.ledgerbridge.settlement.blockchain.SettlementLedgerService;
import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.reconciliation.ReconciliationResult;
import com.ledgerbridge.settlement.reconciliation.ReconciliationService;
import com.ledgerbridge.settlement.repository.MoneyMovementTransactionRepository;
import com.ledgerbridge.settlement.analytics.BigQuerySettlementExportService;
import com.ledgerbridge.settlement.analytics.BigQuerySettlementExportService.DailySettlementMetric;
import com.ledgerbridge.settlement.analytics.SettlementAnalyticsRepository;
import com.ledgerbridge.settlement.analytics.SettlementAnalyticsRepository.SettlementAnalyticsSummary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SettlementOperations {

    private final MoneyMovementTransactionRepository transactionRepository;
    private final SettlementLedgerService settlementLedgerService;
    private final ReconciliationService reconciliationService;
    private final SettlementAnalyticsRepository analyticsRepository;
    private final BigQuerySettlementExportService bigQuerySettlementExportService;  

    public SettlementOperations(
            MoneyMovementTransactionRepository transactionRepository,
            SettlementLedgerService settlementLedgerService,
            ReconciliationService reconciliationService,
            SettlementAnalyticsRepository analyticsRepository,
            BigQuerySettlementExportService bigQuerySettlementExportService) {

        this.transactionRepository = transactionRepository;
        this.settlementLedgerService = settlementLedgerService;
        this.reconciliationService = reconciliationService;
        this.analyticsRepository = analyticsRepository;
        this.bigQuerySettlementExportService = bigQuerySettlementExportService;
    }

    @Tool(description = "Retrieve a money movement transaction by transaction ID")
    public MoneyMovementTransaction getTransaction(UUID transactionId) {

        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Transaction not found: " + transactionId));
    }

    @Tool(description = "Retrieve the blockchain settlement associated with a money movement transaction")
    public TransactionLedgerResponse getTransactionLedger(UUID transactionId) {
        getTransaction(transactionId);

        try {
            LedgerSettlement settlement =
                    settlementLedgerService.getSettlement(
                            transactionId.toString());

            return new TransactionLedgerResponse(
                    settlement.transactionId(),
                    settlement.sourceAccount(),
                    settlement.destinationAccount(),
                    settlement.formattedAmount(),
                    settlement.currency(),
                    settlement.timestamp()
            );

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to retrieve blockchain settlement for transaction: "
                            + transactionId,
                    exception);
        }
    }

    @Tool(description = "Reconcile a database transaction against its blockchain settlement")
    public ReconciliationResult reconcileTransaction(
            UUID transactionId) {

        return reconciliationService.reconcile(transactionId);
    }

    public record TransactionLedgerResponse(
        String transactionId,
        String sourceAccount,
        String destinationAccount,
        String amount,
        String currency,
        long timestamp) {
    }

    @Tool(description = """
        Retrieve aggregate settlement analytics from PostgreSQL,
        including total transaction count, counts by status,
        total amounts by currency, failed transactions,
        and disputed transactions. This operation is read-only.
        """)
    public SettlementAnalyticsSummary getSettlementAnalytics() {
        return analyticsRepository.getSummary();
    }

    @Tool(description = """
        Retrieve daily settlement metrics from BigQuery, including
        settlement date, currency, transaction status, transaction count,
        total amount, failed transaction count, and disputed transaction
        count. This operation is read-only.
        """)
    public List<DailySettlementMetric> getDailySettlementMetrics() {
        return bigQuerySettlementExportService.getDailySettlementMetrics();
    }
}