package com.jexis.jexis_backend.subscription.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class LocalSubscriptionOperationException extends DomainException {
    public LocalSubscriptionOperationException() {
        super(400, "LOCAL_SUBSCRIPTION_OPERATION", "Billing operations are not supported for the local free plan");
    }
}
