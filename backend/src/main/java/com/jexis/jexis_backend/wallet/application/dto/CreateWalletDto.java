package com.jexis.jexis_backend.wallet.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * CreateWalletDto
 *
 * Data Transfer Object used for wallet creation requests. It encapsulates the
 * necessary data required to create a new wallet, such as the wallet name and
 * owner ID.
 *
 * Author: Leo
 */
public class CreateWalletDto {
    @NotNull(message = "Account id is required")
    private UUID accountId;

    @NotNull(message = "Wallet name is required")
    private String name;

    public UUID getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }
}
