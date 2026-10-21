package com.jexis.jexis_backend.plan.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class PlanNotPublishedException extends DomainException {
    public PlanNotPublishedException() {
        super(
                HttpStatus.BAD_REQUEST.value(),
                "PLAN_NOT_PUBLISHED",
                "Plan is not published"
        );
    }
}
