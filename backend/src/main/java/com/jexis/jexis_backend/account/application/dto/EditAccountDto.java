package com.jexis.jexis_backend.account.application.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * EditAccountDto
 *
 * Data Transfer Object used for account editing requests. It encapsulates the
 * necessary data required to edit an existing account, such as the account ID
 * and updated account details.
 *
 * Author: Leo
 */
public class EditAccountDto {
    @NotBlank(message = "Account name cannot be blank")
    @Size(min = 1, max = 100, message = "Account name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Owner ID cannot be null")
    private UUID ownerId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

}
