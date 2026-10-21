package com.jexis.jexis_backend.member.application.dto;

import com.jexis.jexis_backend.member.domain.enums.Role;
import jakarta.validation.constraints.NotNull;

public record EditMemberDto(
        @NotNull(message = "Role cannot be null")
        Role role) {
}
