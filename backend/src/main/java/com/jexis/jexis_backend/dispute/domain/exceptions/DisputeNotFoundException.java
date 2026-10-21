package com.jexis.jexis_backend.dispute.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class DisputeNotFoundException extends DomainException {
    public DisputeNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "DISPUTE_NOT_FOUND", "Dispute not found");
    }
}
