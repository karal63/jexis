package com.jexis.jexis_backend.plan.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class PlanCannotBePublishedException extends DomainException {
    public PlanCannotBePublishedException() {
        super(
                HttpStatus.BAD_REQUEST.value(),
                "PLAN_CANNOT_BE_PUBLISHED",
                "Plan must have a default price before it can be published"
        );
    }
}
