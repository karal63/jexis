package com.jexis.jexis_backend.cardholder.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.account.application.dto.AccountAdminResponseDto;
import com.jexis.jexis_backend.common.dto.SpendingLimit;
import com.jexis.jexis_backend.user.application.dto.UserAdminResponseDto;

public record CardHolderAdminResponseDto(
        UUID id,
        String stripeCardHolderId,
        AccountAdminResponseDto account,
        UserAdminResponseDto user,
        String name,
        String billingAddressLine1,
        String billingAddressLine2,
        String billingCity,
        String billingState,
        String billingCountry,
        String billingPostalCode,
        List<SpendingLimit> spendingLimits,
        String status,
        Boolean isDeleted,
        LocalDateTime deletedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
