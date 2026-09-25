package com.ledgerbridge.settlement.reconciliation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reconciliation")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(
            ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ReconciliationResult> reconcile(
            @PathVariable UUID transactionId) {

        return ResponseEntity.ok(
                reconciliationService.reconcile(transactionId));
    }
}