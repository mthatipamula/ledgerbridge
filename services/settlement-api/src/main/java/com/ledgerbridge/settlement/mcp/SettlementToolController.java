package com.ledgerbridge.settlement.mcp;

import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.reconciliation.ReconciliationResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tools")
public class SettlementToolController {

    private final SettlementOperations settlementOperations;

    public SettlementToolController(
            SettlementOperations settlementOperations) {
        this.settlementOperations = settlementOperations;
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
}