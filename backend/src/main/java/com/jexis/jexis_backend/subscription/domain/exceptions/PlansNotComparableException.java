package com.jexis.jexis_backend.subscription.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class PlansNotComparableException extends DomainException {
    public PlansNotComparableException() {
        super(
                HttpStatus.BAD_REQUEST.value(),
                "PLANS_NOT_COMPARABLE",
                "Plans must use comparable recurring prices and have different effective costs"
        );
    }
}
