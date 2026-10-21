package com.jexis.jexis_backend.externalAccount.application.dto;

import java.util.List;

public record ExternalAccountPageResponseDto(
        List<ExternalAccountResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        long totalPages) {
}
