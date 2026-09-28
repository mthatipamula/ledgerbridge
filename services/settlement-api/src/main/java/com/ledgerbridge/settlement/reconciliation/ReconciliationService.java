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

        System.out.println("=== Reconciliation Debug ===");

        System.out.println("DB transaction ID: " + transaction.getId());
        System.out.println("Blockchain transaction ID: "
                + ledgerSettlement.transactionId());

        System.out.println("DB source: " + transaction.getSourceAccountId());
        System.out.println("Blockchain source: "
                + ledgerSettlement.sourceAccount());

        System.out.println("DB destination: "
                + transaction.getDestinationAccountId());
        System.out.println("Blockchain destination: "
                + ledgerSettlement.destinationAccount());

        System.out.println("DB amount minor units: "
                + transaction.getAmount()
                        .movePointRight(2)
                        .toBigIntegerExact());
        System.out.println("Blockchain amount: " + ledgerSettlement.amount());

        System.out.println("DB currency: " + transaction.getCurrency());
        System.out.println("Blockchain currency: "
                + ledgerSettlement.currency());

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
                        .equals(BigInteger.valueOf(ledgerSettlement.amount()))
                        && transaction.getCurrency()
                        .equals(ledgerSettlement.currency());

        System.out.println("Reconciliation matched: " + matched);

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