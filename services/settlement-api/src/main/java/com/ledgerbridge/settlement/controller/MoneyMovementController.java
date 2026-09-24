package com.ledgerbridge.settlement.controller;

import com.ledgerbridge.settlement.blockchain.LedgerSettlement;
import com.ledgerbridge.settlement.blockchain.SettlementLedgerService;
import com.ledgerbridge.settlement.domain.MoneyMovementTransaction;
import com.ledgerbridge.settlement.service.MoneyMovementService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/money-movements")
public class MoneyMovementController {

    private final MoneyMovementService moneyMovementService;

    private final SettlementLedgerService settlementLedgerService;

    public MoneyMovementController(MoneyMovementService moneyMovementService, SettlementLedgerService settlementLedgerService)   {
        this.moneyMovementService = moneyMovementService;
        this.settlementLedgerService = settlementLedgerService;
    }

    @PostMapping
    public ResponseEntity<MoneyMovementTransaction> createMoneyMovement(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateMoneyMovementRequest request) {

        MoneyMovementTransaction transaction =
                moneyMovementService.createTransaction(
                        idempotencyKey,
                        request.sourceAccountId(),
                        request.destinationAccountId(),
                        request.amount(),
                        request.currency());

        URI location = URI.create(
                "/api/v1/money-movements/" + transaction.getId());

        return ResponseEntity
                .created(location)
                .body(transaction);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<MoneyMovementTransaction> getMoneyMovement(
                @PathVariable UUID transactionId) {

        return ResponseEntity.ok(
                moneyMovementService.getTransaction(transactionId)
        );
    }

    @GetMapping("/{transactionId}/ledger")
public ResponseEntity<LedgerSettlement> getLedgerSettlement(
        @PathVariable UUID transactionId) {

    try {
        LedgerSettlement settlement =
                settlementLedgerService.getSettlement(
                        transactionId.toString());

        return ResponseEntity.ok(settlement);

    } catch (Exception exception) {
        throw new IllegalArgumentException(
                "Unable to retrieve blockchain settlement: "
                        + transactionId,
                exception);
    }
}
}