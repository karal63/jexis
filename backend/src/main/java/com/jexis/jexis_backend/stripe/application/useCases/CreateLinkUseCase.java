package com.jexis.jexis_backend.stripe.application.useCases;

import org.springframework.stereotype.Service;

import com.stripe.StripeClient;
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

    public CreateLinkUseCase(StripeClient stripe) {
        this.stripe = stripe;
    }

    /**
     * Creates a new account link
     * Accepts an account ID from the controller, creates a new account link,
     * and returns the created account link.
     * @param accountId the ID of the account for which to create a link
     * @return the created account link
     */
    public AccountLink execute(String accountId, AccountLinkCreateParams.Type type) {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(accountId)
                    .setRefreshUrl("https://example.com")
                    .setReturnUrl("https://example.com?accountId=" + accountId)
                    .setType(type)
                    .build();

            return stripe.v1().accountLinks().create(params);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
