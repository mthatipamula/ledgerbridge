package com.ledgerbridge.settlement.mcp;

import com.ledgerbridge.settlement.blockchain.LedgerSettlement;
import com.ledgerbridge.settlement.blockchain.SettlementLedgerService;
import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.reconciliation.ReconciliationResult;
import com.ledgerbridge.settlement.reconciliation.ReconciliationService;
import com.ledgerbridge.settlement.repository.MoneyMovementTransactionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SettlementOperations {

    private final MoneyMovementTransactionRepository transactionRepository;
    private final SettlementLedgerService settlementLedgerService;
    private final ReconciliationService reconciliationService;

    public SettlementOperations(
            MoneyMovementTransactionRepository transactionRepository,
            SettlementLedgerService settlementLedgerService,
            ReconciliationService reconciliationService) {

        this.transactionRepository = transactionRepository;
        this.settlementLedgerService = settlementLedgerService;
        this.reconciliationService = reconciliationService;
    }

    public MoneyMovementTransaction getTransaction(UUID transactionId) {

        return transactionRepository.findById(transactionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Transaction not found: " + transactionId));
    }

    public LedgerSettlement getTransactionLedger(UUID transactionId) {

        getTransaction(transactionId);

        try {
            return settlementLedgerService.getSettlement(
                    transactionId.toString());

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to retrieve blockchain settlement for transaction: "
                            + transactionId,
                    exception);
        }
    }

    public ReconciliationResult reconcileTransaction(
            UUID transactionId) {

        return reconciliationService.reconcile(transactionId);
    }
}