package com.ledgerbridge.settlement.controller;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateMoneyMovementRequest(

        @NotBlank(message = "sourceAccountId must not be blank")
        String sourceAccountId,

        @NotBlank(message = "destinationAccountId must not be blank")
        String destinationAccountId,

        @NotNull(message = "amount must not be null")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,

        @NotBlank(message = "currency must not be blank")
        String currency
) {
}