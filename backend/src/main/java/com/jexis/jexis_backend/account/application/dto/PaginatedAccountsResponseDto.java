package com.jexis.jexis_backend.account.application.dto;

import java.util.List;

public record PaginatedAccountsResponseDto(
        List<AccountResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages) {
}
