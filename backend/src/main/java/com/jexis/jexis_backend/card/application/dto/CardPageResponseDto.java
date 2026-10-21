package com.jexis.jexis_backend.card.application.dto;

import java.util.List;

public record CardPageResponseDto(
        List<CardResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        long totalPages) {
}
