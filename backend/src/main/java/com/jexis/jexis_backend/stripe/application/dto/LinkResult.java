package com.jexis.jexis_backend.stripe.application.dto;

/**
 * LinkResult
 *
 * Data Transfer Object used for link creation requests. It encapsulates the
 * necessary data required to create a new link, such as the URL.
 *
 * Author: Leo
 */
public record LinkResult(String url) {

}
