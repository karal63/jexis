package com.jexis.jexis_backend.stripe.application.dto;

/**
 * CreateLinkDto
 *
 * Data Transfer Object used for link creation requests. It encapsulates the
 * necessary data required to create a new link, such as the account ID.
 *
 * Author: Leo
 */
public record CreateLinkDto(String accountId) {
}
