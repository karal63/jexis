package com.jexis.jexis_backend.dispute.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class DisputeExistsException extends DomainException {
    public DisputeExistsException() {
        super(HttpStatus.CONFLICT.value(), "DISPUTE_EXISTS", "Dispute already exists for this transaction");
    }
}
