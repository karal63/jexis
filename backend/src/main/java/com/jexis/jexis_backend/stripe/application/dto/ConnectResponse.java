package com.jexis.jexis_backend.stripe.application.dto;

/**
 * ConnectResponse
 *
 * Data Transfer Object used for Connect requests. It encapsulates the
 * necessary data required to create a new Connect account, such as the
 * account ID.
 *
 * Author: Leo
 */
public record ConnectResponse(String accountId) {

}
