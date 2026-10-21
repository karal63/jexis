package com.jexis.jexis_backend.user.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

/**
 * EmailExistsException
 *
 * This exception is thrown when the provided email already exists. It
 * extends {@link DomainException} to provide a specific error status, code and
 * message, which are then handled by the global exception handler to return a
 * structured error response to the client.
 *
 * Author: Leo
 */
public class EmailExistsException extends DomainException {
    public EmailExistsException() {
        super(HttpStatus.CONFLICT.value(), "EMAIL_EXISTS", "Email already exists");
    }
}