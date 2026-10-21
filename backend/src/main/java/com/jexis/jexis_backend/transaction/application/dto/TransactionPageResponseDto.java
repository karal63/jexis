package com.jexis.jexis_backend.transaction.application.dto;

import java.util.List;

public record TransactionPageResponseDto(
        List<TransactionResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        long totalPages) {
}
