package com.jexis.jexis_backend.card.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

/**
 * CardNotFoundException
 *
 * This exception is thrown when the provided card is not found. It
 * extends {@link DomainException} to provide a specific error status, code and
 * message, which are then handled by the global exception handler to return a
 * structured error response to the client.
 *
 * Author: Leo
 */
public class CardNotFoundException extends DomainException {
    public CardNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "CARD_NOT_FOUND", "Card not found");
    }
}
