package com.jexis.jexis_backend.user.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

/**
 * UserNotFoundException
 *
 * This exception is thrown when the provided user is not found. It
 * extends {@link DomainException} to provide a specific error status, code and
 * message, which are then handled by the global exception handler to return a
 * structured error response to the client.
 *
 * Author: Leo
 */
public class UserNotFoundException extends DomainException {
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "USER_NOT_FOUND", "User not found");
    }
}
