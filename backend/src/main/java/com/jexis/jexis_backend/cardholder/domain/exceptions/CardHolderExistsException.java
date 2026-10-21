package com.jexis.jexis_backend.cardholder.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class CardHolderExistsException extends DomainException {
    public CardHolderExistsException(String email) {
        super(HttpStatus.CONFLICT.value(), "EMAIL_EXISTS", "Card holder with email " + email + " in this account already exists");
    }
}
