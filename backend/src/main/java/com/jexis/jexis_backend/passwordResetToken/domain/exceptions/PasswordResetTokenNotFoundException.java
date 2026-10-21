package com.jexis.jexis_backend.passwordResetToken.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

public class PasswordResetTokenNotFoundException extends DomainException {
    public PasswordResetTokenNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "PASSWORD_RESET_TOKEN_NOT_FOUND", "Password reset token not found");
    }
}
