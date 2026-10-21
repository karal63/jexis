package com.jexis.jexis_backend.wallet.domain.exceptions;

/**
 * ForbiddenException
 *
 * This exception is thrown when the user is not allowed to perform the
 * requested action.
 *
 * Author: Leo
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException() {
        super("You don't have permission to access this resource");
    }
}
