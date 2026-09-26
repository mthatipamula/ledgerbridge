package com.ledgerbridge.settlement.blockchain;

public record LedgerSettlement(
        String transactionId,
        String sourceAccount,
        String destinationAccount,
        long amount,
        String currency,
        long timestamp) {

    public String formattedAmount() {
        return String.format("%.2f %s", amount / 100.0, currency);
    }
}