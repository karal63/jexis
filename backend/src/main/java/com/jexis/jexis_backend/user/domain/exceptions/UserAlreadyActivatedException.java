package com.jexis.jexis_backend.user.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class UserAlreadyActivatedException extends DomainException {
    public UserAlreadyActivatedException() {
        super(HttpStatus.BAD_REQUEST.value(), "USER_ALREADY_ACTIVATED", "User is already activated.");
    }
}
