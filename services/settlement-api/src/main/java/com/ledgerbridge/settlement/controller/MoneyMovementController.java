package com.ledgerbridge.settlement.controller;

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

    public MoneyMovementController(MoneyMovementService moneyMovementService) {
        this.moneyMovementService = moneyMovementService;
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
}