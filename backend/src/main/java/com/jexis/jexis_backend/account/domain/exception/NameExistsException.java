package com.jexis.jexis_backend.account.domain.exception;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

/**
 * Returns an error response when an account with the same name already exists.
 *
 * This exception is thrown when account name was found in the database. It
 * extends {@link DomainException} to provide a specific error status, code and
 * message, which are then handled by the global exception handler to return a
 * structured error response to the client.
 *
 * Author: Leo
 */
public class NameExistsException extends DomainException {
    public NameExistsException(String name) {
        super(HttpStatus.CONFLICT.value(), "NAME_EXISTS", "Name " + name + " already exists");
    }
}