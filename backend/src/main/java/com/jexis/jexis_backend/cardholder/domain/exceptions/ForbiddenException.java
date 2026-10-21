package com.jexis.jexis_backend.cardholder.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class ForbiddenException extends DomainException {
    public ForbiddenException() {
        super(HttpStatus.FORBIDDEN.value(), "FORBIDDEN", "Access denied");
    }
}
