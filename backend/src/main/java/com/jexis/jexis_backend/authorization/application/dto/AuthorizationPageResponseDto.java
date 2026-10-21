package com.jexis.jexis_backend.authorization.application.dto;

import java.util.List;

public record AuthorizationPageResponseDto(
        List<AuthorizationResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        long totalPages
) {
}
