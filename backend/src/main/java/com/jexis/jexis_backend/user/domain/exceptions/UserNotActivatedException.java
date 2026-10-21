package com.jexis.jexis_backend.user.domain.exceptions;

import com.jexis.jexis_backend.common.web.error.DomainException;
import org.springframework.http.HttpStatus;

public class UserNotActivatedException extends DomainException {
    public UserNotActivatedException() {
        super(HttpStatus.BAD_REQUEST.value(), "USER_NOT_ACTIVATED", "User is not activated");
    }
}
