package com.ofektom.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for wallet-to-wallet transfers.
 */
public record TransferRequest(
    @NotBlank(message = "Sender wallet ID is required")
    String senderWalletId,
    
    @NotBlank(message = "Receiver wallet ID is required")
    String receiverWalletId,
    
    @NotNull(message = "Amount in minor units is required")
    @Positive(message = "Amount must be positive")
    Long amountInMinorUnits,
    
    @NotBlank(message = "Idempotency key is required")
    String idempotencyKey
) {
}

