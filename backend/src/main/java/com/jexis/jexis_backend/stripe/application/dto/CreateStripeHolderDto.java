package com.jexis.jexis_backend.stripe.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * CreateCardHolderDto
 *
 * Data Transfer Object used for card holder creation requests. It encapsulates
 * the
 * necessary data required to create a new card holder, such as the connected
 * account ID.
 *
 * @Author: Leo
 */
public record CreateStripeHolderDto(
        @NotBlank String connectedAccountId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank String phoneNumber,
        @NotBlank String addressLine1,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String country,
        @NotBlank String postalCode) {
}
