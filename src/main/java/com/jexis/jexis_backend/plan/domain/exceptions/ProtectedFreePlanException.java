package com.jexis.jexis_backend.plan.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class ProtectedFreePlanException extends DomainException {
    public ProtectedFreePlanException() {
        super(400, "PROTECTED_FREE_PLAN", "The system free plan code and pricing cannot be changed or deleted");
    }
}
