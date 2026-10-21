package com.jexis.jexis_backend.account.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.jexis.jexis_backend.user.application.dto.UserResponseDto;

public record AccountResponseDto(
        UUID id,
        String firstName,
        String lastName,
        String city,
        String country,
        String line1,
        String line2,
        String postalCode,
        String state,
        String phone,
        String email,
        String connectAccountId,
        String accountLink,
        UserResponseDto owner,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
