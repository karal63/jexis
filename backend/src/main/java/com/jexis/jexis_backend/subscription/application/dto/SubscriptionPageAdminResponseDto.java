package com.jexis.jexis_backend.subscription.application.dto;

import java.util.List;

public record SubscriptionPageAdminResponseDto(
        List<SubscriptionResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages
) {
}
