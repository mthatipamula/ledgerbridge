package com.ledgerbridge.settlement.event;

import java.math.BigDecimal;
import java.util.UUID;

public record SettlementRequestedEvent(
        UUID transactionId,
        String sourceAccountId,
        String destinationAccountId,
        BigDecimal amount,
        String currency) {
}