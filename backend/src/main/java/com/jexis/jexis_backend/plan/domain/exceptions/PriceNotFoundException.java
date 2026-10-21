package com.jexis.jexis_backend.plan.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class PriceNotFoundException extends DomainException {
    public PriceNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "PRICE_NOT_FOUND", "Price not found");
    }
}
