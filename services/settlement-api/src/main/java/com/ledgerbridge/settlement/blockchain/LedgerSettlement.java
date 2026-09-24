package com.ledgerbridge.settlement.blockchain;

import java.math.BigInteger;

public record LedgerSettlement(
        String transactionId,
        String sourceAccount,
        String destinationAccount,
        BigInteger amount,
        String currency,
        BigInteger timestamp) {
}