package com.jexis.jexis_backend.card.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;

/**
 * ForbiddenException
 *
 * This exception is thrown when the user is not allowed to perform the
 * requested action. It
 * extends {@link DomainException} to provide a specific error status, code and
 * message, which are then handled by the global exception handler to return a
 * structured error response to the client.
 *
 * Author: Leo
 */
public class ForbiddenException extends DomainException {
    public ForbiddenException() {
        super(403, "FORBIDDEN", "Forbidden");
    }
}
