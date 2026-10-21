package com.jexis.jexis_backend.account.domain.exception;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class ResourceLimitException extends DomainException {
    public ResourceLimitException(int status, String code, String message) {
        super(status, code, message);
    }
}
