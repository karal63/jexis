package com.jexis.jexis_backend.subscription.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class PendingSubscriptionOperationConflictException extends DomainException {
    public PendingSubscriptionOperationConflictException(String code, String message) {
        super(HttpStatus.CONFLICT.value(), code, message);
    }
}
