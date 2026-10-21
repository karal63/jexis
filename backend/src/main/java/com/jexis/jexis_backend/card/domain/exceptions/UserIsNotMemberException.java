package com.jexis.jexis_backend.card.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class UserIsNotMemberException extends DomainException {
    public UserIsNotMemberException() {
        super(HttpStatus.BAD_REQUEST.value(), "USER_NOT_A_MEMBER", "User is not an account member");
    }
}
