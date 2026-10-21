package com.jexis.jexis_backend.cardholder.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.account.application.dto.AccountResponseDto;
import com.jexis.jexis_backend.cardholder.domain.enums.CardHolderStatus;
import com.jexis.jexis_backend.common.dto.SpendingLimit;
import com.jexis.jexis_backend.user.application.dto.UserResponseDto;

public record CardHolderResponseDto(
        UUID id,
        AccountResponseDto account,
        UserResponseDto user,
        String name,
        String billingAddressLine1,
        String billingAddressLine2,
        String billingCity,
        String billingState,
        String billingCountry,
        String billingPostalCode,
        List<SpendingLimit> spendingLimits,
        CardHolderStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
