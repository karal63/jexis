package com.jexis.jexis_backend.user.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.user.domain.enums.UserRole;

public record UserResponseDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        List<UserRole> roles,
        Boolean isActivated,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
