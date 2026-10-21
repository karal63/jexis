package com.jexis.jexis_backend.dispute.application.dto;

import java.util.List;

public record DisputePageResponseDto(
        List<DisputeResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        long totalPages) {
}
