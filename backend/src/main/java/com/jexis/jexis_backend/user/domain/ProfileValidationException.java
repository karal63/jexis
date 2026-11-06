package com.jexis.jexis_backend.user.domain;

public class ProfileValidationException extends RuntimeException {
    private final String field;
    private final String code;

    public ProfileValidationException(String field, String code) {
        super("Invalid " + field);
        this.field = field;
        this.code = code;
    }

    public String field() { return field; }
    public String code() { return code; }
}
