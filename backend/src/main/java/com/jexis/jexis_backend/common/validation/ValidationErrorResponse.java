package com.jexis.jexis_backend.common.validation;

import com.jexis.jexis_backend.common.web.error.ErrorResponse;

import java.util.List;

public class ValidationErrorResponse extends ErrorResponse {
    private final List<ValidationError> errors;

    public ValidationErrorResponse(Integer status, String code, String message, List<ValidationError> errors) {
        super(status, code, message);
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
