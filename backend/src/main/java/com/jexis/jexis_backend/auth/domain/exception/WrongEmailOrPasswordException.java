package com.jexis.jexis_backend.auth.domain.exception;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class WrongEmailOrPasswordException extends DomainException {
    public WrongEmailOrPasswordException() {
        super(HttpStatus.BAD_REQUEST.value(), "INVALID_EMAIL_OR_PASSWORD", "Invalid email or password");
    }
}
