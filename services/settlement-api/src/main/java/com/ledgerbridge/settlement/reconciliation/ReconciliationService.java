package com.ledgerbridge.settlement.reconciliation;

import com.ledgerbridge.settlement.blockchain.LedgerSettlement;
import com.ledgerbridge.settlement.blockchain.SettlementLedgerService;
import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.repository.MoneyMovementTransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.UUID;

@Service
public class ReconciliationService {

    private final MoneyMovementTransactionRepository transactionRepository;
    private final SettlementLedgerService settlementLedgerService;

    public ReconciliationService(
            MoneyMovementTransactionRepository transactionRepository,
            SettlementLedgerService settlementLedgerService) {

        this.transactionRepository = transactionRepository;
        this.settlementLedgerService = settlementLedgerService;
    }

    public ReconciliationResult reconcile(UUID transactionId) {

        MoneyMovementTransaction transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transaction not found: "
                                                + transactionId));
       LedgerSettlement ledgerSettlement;                                
       try {
                ledgerSettlement =
                        settlementLedgerService.getSettlement(
                                transactionId.toString());
        } catch (Exception exception) {
                throw new IllegalStateException(
                        "Unable to retrieve blockchain settlement for transaction: "
                                + transactionId,
                        exception);
        }

        boolean matched =
                transaction.getId().toString()
                        .equals(ledgerSettlement.transactionId())
                        && transaction.getSourceAccountId()
                        .equals(ledgerSettlement.sourceAccount())
                        && transaction.getDestinationAccountId()
                        .equals(ledgerSettlement.destinationAccount())
                        && transaction.getAmount()
                        .movePointRight(2)
                        .toBigIntegerExact()
                        .equals(ledgerSettlement.amount())
                        && transaction.getCurrency()
                        .equals(ledgerSettlement.currency());

        String message = matched
                ? "Database transaction matches blockchain settlement"
                : "Database transaction does not match blockchain settlement";

        return new ReconciliationResult(
                transaction.getId(),
                transaction.getStatus().name(),
                matched ? "CONFIRMED" : "MISMATCH",
                matched,
                message);
    }
}