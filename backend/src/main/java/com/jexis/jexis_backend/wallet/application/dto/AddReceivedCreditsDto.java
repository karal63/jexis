package com.jexis.jexis_backend.wallet.application.dto;

import com.jexis.jexis_backend.wallet.domain.entities.ReceivedCreditsNetwork;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AddReceivedCreditsDto(
        @NotNull(message = "Amount cannot be null")
        @Positive(message = "Amount must be positive")
        Long amount,
        
        @NotNull(message = "Currency cannot be null")
        @Size(min = 3, max = 3, message = "Currency must be 3 characters (e.g., USD)")
        String currency,
        
        @NotNull(message = "Network cannot be null")
        ReceivedCreditsNetwork network,
        
        String description
) {

}
