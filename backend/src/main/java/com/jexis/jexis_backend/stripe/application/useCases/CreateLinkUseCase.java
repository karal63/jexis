package com.jexis.jexis_backend.stripe.application.useCases;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountLinkCreateParams;

/**
 * CreateLinkUseCase
 * This service class implements the use case for creating a new account link.
 * It contains only the business logic related to account link creation
 * Author: Leo
 */
@Service
public class CreateLinkUseCase {
    private final StripeClient stripe;

    @Value("${application.origin}")
    private String applicationOrigin;

    public CreateLinkUseCase(StripeClient stripe) {
        this.stripe = stripe;
    }

    /**
     * Creates a new account link
     * Accepts an account ID from the controller, creates a new account link,
     * and returns the created account link.
     * @return the created account link
     */
    public AccountLink execute(String connectAccountId, UUID accountId, AccountLinkCreateParams.Type type) {
        String callbackUrl = UriComponentsBuilder.fromUriString(applicationOrigin)
                .pathSegment("app", "accounts", accountId.toString())
                .build().toUriString();
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(connectAccountId)
                    .setRefreshUrl(callbackUrl)
                    .setReturnUrl(callbackUrl)
                    .setType(type)
                    .build();

            return stripe.v1().accountLinks().create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe account link", e);
        }
    }
}
