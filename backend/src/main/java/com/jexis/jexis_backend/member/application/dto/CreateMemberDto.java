package com.jexis.jexis_backend.member.application.dto;

import java.util.UUID;

import com.jexis.jexis_backend.member.domain.enums.Role;

import jakarta.validation.constraints.NotNull;

public record CreateMemberDto(
        @NotNull UUID accountId,
        @NotNull UUID userId,
        @NotNull Role role

) {

}
