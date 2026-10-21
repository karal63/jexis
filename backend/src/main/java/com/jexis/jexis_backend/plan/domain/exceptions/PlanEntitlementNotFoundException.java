package com.jexis.jexis_backend.plan.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class PlanEntitlementNotFoundException extends DomainException {
    public PlanEntitlementNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "PLAN_ENTITLEMENT_NOT_FOUND", "Plan entitlement not found");
    }
}
