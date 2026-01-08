package com.ofektom.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for transaction information.
 */
public record TransactionResponse(
    String transactionId,
    String walletId,
    String transactionType,
    Long amountInMinorUnits,
    BigDecimal amountInMajorUnits,
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {
}

