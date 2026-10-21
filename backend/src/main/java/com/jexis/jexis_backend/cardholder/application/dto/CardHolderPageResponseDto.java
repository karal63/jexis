package com.jexis.jexis_backend.cardholder.application.dto;

import java.util.List;

public record CardHolderPageResponseDto(
        List<CardHolderResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        long totalPages) {
}
