package com.ofektom.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionRequest(
    @NotBlank(message = "Wallet ID is required")
    String walletId,
    
    @NotBlank(message = "Transaction type (CREDIT/DEBIT) is required")
    String type,
    
    @NotNull(message = "Amount in minor units is required")
    @Positive(message = "Amount must be positive")
    Long amountInMinorUnits,
    
    @NotBlank(message = "Idempotency key is required")
    String idempotencyKey
) {
}

