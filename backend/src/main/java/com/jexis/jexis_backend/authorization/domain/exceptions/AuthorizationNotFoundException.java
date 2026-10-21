package com.jexis.jexis_backend.authorization.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class AuthorizationNotFoundException extends DomainException {
    public AuthorizationNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "AUTHORIZATION_NOT_FOUND", "Authorization not found");
    }
}
