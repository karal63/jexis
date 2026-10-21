package com.jexis.jexis_backend.member.domain.exceptions;

import org.springframework.http.HttpStatus;

import com.jexis.jexis_backend.common.web.error.DomainException;

public class MemberNotFoundException extends DomainException {
    public MemberNotFoundException() {
        super(HttpStatus.NOT_FOUND.value(), "MEMBER_NOT_FOUND", "Member not found");
    }
}
