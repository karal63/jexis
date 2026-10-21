package com.jexis.jexis_backend.auth.domain.exception;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordResetTokenException extends DomainException {
    public InvalidPasswordResetTokenException() {
        super(HttpStatus.BAD_REQUEST.value(), "INVALID_PASSWORD_RESET_TOKEN", "Invalid password reset token");
    }
}
