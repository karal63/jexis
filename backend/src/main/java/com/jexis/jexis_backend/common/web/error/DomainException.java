package com.jexis.jexis_backend.common.web.error;

/**
 * Base exception for all domain-related errors in the application.
 *
 * This abstract class serves as the parent for all custom exceptions that
 * represent specific error conditions within the application's domain logic. It
 * extends RuntimeException, allowing it to be thrown without being declared in
 * method signatures.
 *
 * Author: Leo
 */
public abstract class DomainException extends RuntimeException {
    private final String code;
    private final Integer status;

    protected DomainException(Integer status, String code, String message) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public Integer getStatus() {
        return status;
    }
}
