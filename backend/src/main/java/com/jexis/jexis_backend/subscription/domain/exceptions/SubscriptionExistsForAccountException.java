package com.jexis.jexis_backend.subscription.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class SubscriptionExistsForAccountException extends DomainException {
    public SubscriptionExistsForAccountException() {
        super(HttpStatus.BAD_REQUEST.value(), "SUBSCRIPTION_EXISTS_FOR_ACCOUNT", "Subscription already exists for account");
    }
}
