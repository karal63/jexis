package com.jexis.jexis_backend.plan.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class PlanNotFoundException extends DomainException {
    public PlanNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "PLAN_NOT_FOUND", "Plan not found");
    }
}
