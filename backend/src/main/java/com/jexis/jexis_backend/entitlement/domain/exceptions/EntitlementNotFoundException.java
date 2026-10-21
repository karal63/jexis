package com.jexis.jexis_backend.entitlement.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class EntitlementNotFoundException extends DomainException {
    public EntitlementNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "ENTITLEMENT_NOT_FOUND", "Entitlement not found");
    }
}
