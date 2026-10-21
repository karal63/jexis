package com.jexis.jexis_backend.wallet.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * EditWalletDto
 *
 * Data Transfer Object used for wallet editing requests. It encapsulates the
 * necessary data required to edit an existing wallet, such as the wallet name
 * and
 * owner ID.
 *
 * Author: Leo
 */
public class EditWalletDto {
    @NotBlank(message = "Wallet name cannot be blank")
    @Size(min = 1, max = 100, message = "Wallet name must be between 1 and 100 characters")
    private String name;

    public String getName() {
        return name;
    }

}
