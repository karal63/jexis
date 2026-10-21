package com.jexis.jexis_backend.account.domain.exception;

import java.util.UUID;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

/**
 * Returns an error response when account with given id is not found
 *
 * This exception is thrown when account name was not found in the database. It
 * extends {@link DomainException} to provide a specific error status, code and
 * message, which are then handled by the global exception handler to return a
 * structured error response to the client.
 *
 * Author: Leo
 */
public class AccountNotFoundException extends DomainException {
    public AccountNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "ACCOUNT_NOT_FOUND", "Account not found");
    }
}
