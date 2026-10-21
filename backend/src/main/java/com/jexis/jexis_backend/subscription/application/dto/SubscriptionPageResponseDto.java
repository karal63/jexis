package com.jexis.jexis_backend.subscription.application.dto;

import java.util.List;

public record SubscriptionPageResponseDto(
        List<SubscriptionResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages
) {
}
