package com.jexis.jexis_backend.member.application.dto;

import java.util.UUID;

import com.jexis.jexis_backend.account.application.dto.AccountResponseDto;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.user.application.dto.UserResponseDto;

public record MemberResponseDto(UUID id, AccountResponseDto account, UserResponseDto user, Role role) {

}
