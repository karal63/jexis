package com.jexis.jexis_backend.member.application.dto;

import java.util.UUID;

import com.jexis.jexis_backend.account.application.dto.AccountAdminResponseDto;
import com.jexis.jexis_backend.user.application.dto.UserAdminResponseDto;

public record MemberAdminResponseDto(
        UUID id,
        AccountAdminResponseDto account,
        UserAdminResponseDto user,
        String role) {

}
