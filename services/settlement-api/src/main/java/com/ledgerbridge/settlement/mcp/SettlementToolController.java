
package com.ledgerbridge.settlement.mcp;

import com.ledgerbridge.settlement.analytics.BigQuerySettlementExportService;
import com.ledgerbridge.settlement.analytics.SettlementAnalyticsRepository.SettlementAnalyticsSummary;
import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.reconciliation.ReconciliationResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tools")
public class SettlementToolController {

    private final SettlementOperations settlementOperations;
    private final BigQuerySettlementExportService bigQuerySettlementExportService;

    public SettlementToolController(
            SettlementOperations settlementOperations,
            BigQuerySettlementExportService bigQuerySettlementExportService) {
        this.settlementOperations = settlementOperations;
        this.bigQuerySettlementExportService = bigQuerySettlementExportService;
    }

    @GetMapping("/get_transaction/{transactionId}")
    public MoneyMovementTransaction getTransaction(
            @PathVariable UUID transactionId) {
        return settlementOperations.getTransaction(transactionId);
    }

    @GetMapping("/get_transaction_ledger/{transactionId}")
    public SettlementOperations.TransactionLedgerResponse getTransactionLedger(
            @PathVariable UUID transactionId) {
        return settlementOperations.getTransactionLedger(transactionId);
    }

    @GetMapping("/reconcile_transaction/{transactionId}")
    public ReconciliationResult reconcileTransaction(
            @PathVariable UUID transactionId) {
        return settlementOperations.reconcileTransaction(transactionId);
    }

    @GetMapping("/settlement_analytics")
    public SettlementAnalyticsSummary getSettlementAnalytics() {
        return settlementOperations.getSettlementAnalytics();
    }

    @PostMapping("/export_settlement_analytics")
    public BigQuerySettlementExportService.ExportResult exportSettlementAnalytics() {
        return bigQuerySettlementExportService.exportAll();
    }

    @GetMapping("/daily_settlement_metrics")
    public List<BigQuerySettlementExportService.DailySettlementMetric>
            getDailySettlementMetrics() {
        return bigQuerySettlementExportService.getDailySettlementMetrics();
    }
}