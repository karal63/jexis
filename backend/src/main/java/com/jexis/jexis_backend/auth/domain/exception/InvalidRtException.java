package com.jexis.jexis_backend.auth.domain.exception;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

/**
 * InvalidRtException
 *
 * This exception is thrown when the provided refresh token is invalid. It
 * extends {@link DomainException} to provide a specific error status, code and
 * message, which are then handled by the global exception handler to return a
 * structured error response to the client.
 *
 * Author: Leo
 */
public class InvalidRtException extends DomainException {
    public InvalidRtException() {
        super(HttpStatus.BAD_REQUEST.value(), "INVALID_RT", "Invalid refresh token");
    }
}
