package com.jexis.jexis_backend.common.validation;

public record ValidationError(String field, String code, String message) {
}
