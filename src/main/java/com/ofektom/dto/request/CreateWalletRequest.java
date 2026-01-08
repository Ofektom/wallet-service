package com.ofektom.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;

/**
 * Request DTO for wallet creation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreateWalletRequest(
        @Min(value = 0, message = "Initial balance cannot be negative") Long initialBalanceInMinorUnits) {
}
