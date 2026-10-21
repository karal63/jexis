package com.jexis.jexis_backend.cardholder.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class CardHolderNotFoundException extends DomainException {
    public CardHolderNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "CARD_NOT_FOUND", "Card holder not found");
    }
}
