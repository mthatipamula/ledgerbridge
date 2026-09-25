package com.ledgerbridge.settlement.reconciliation;

import java.util.UUID;

public record ReconciliationResult(
        UUID transactionId,
        String databaseStatus,
        String blockchainStatus,
        boolean matched,
        String message) {
}