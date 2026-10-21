package com.jexis.jexis_backend.subscription.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateSubscriptionDto {
    @NotNull
    private UUID planId;

    @NotNull
    private UUID accountId;

    @NotNull
    private String paymentMethodId;
}
