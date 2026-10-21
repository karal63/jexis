package com.jexis.jexis_backend.user.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.user.domain.enums.UserRole;

public record UserAdminResponseDto(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        List<UserRole> roles,
        Boolean isActivated,
        Boolean isDeleted,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
