package com.jexis.jexis_backend.externalAccount.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class ExternalAccountNotFoundException extends DomainException {
    public ExternalAccountNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "EXTERNAL_ACCOUNT_NOT_FOUND", "External account not found");
    }
}
