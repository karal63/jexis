package com.jexis.jexis_backend.member.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class MemberExistsException extends DomainException {
    public MemberExistsException() {
        super(HttpStatus.BAD_REQUEST.value(), "MEMBER_EXISTS", "Member already exists in this account");
    }
}
