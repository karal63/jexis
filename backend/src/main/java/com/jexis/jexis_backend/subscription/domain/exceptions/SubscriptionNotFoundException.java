package com.jexis.jexis_backend.subscription.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class SubscriptionNotFoundException extends DomainException {
    public SubscriptionNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "SUBSCRIPTION_NOT_FOUND", "Subscription not found");
    }
}
