package com.jexis.jexis_backend.subscription.application.dto;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import com.jexis.jexis_backend.user.application.dto.PaymentMethodResponseDto;
import com.jexis.jexis_backend.user.domain.entities.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponseDto(
        UUID id,
        User user,
        Account account,
        Plan plan,
        Price price,
        PaymentMethodResponseDto paymentMethod,
        SubscriptionStatus status,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        boolean cancelAtPeriodEnd,
        LocalDateTime canceledAt,
        Plan scheduledPlan,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
