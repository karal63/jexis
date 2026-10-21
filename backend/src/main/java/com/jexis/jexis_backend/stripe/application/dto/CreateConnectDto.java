package com.jexis.jexis_backend.stripe.application.dto;

/**
 * CreateConnectDto
 *
 * Data Transfer Object used for Connect creation requests. It encapsulates the
 * necessary data required to create a new Connect account, such as the email.
 *
 * Author: Leo
 */
public record CreateConnectDto(String email) {
}
