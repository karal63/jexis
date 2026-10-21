package com.jexis.jexis_backend.subscription.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class ForbiddenException extends DomainException {
    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN.value(), "FORBIDDEN", "Forbidden");
    }
}
