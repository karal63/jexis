package com.jexis.jexis_backend.user.application.dto;

import java.util.List;

public record UserPageResponseDto(
        List<UserResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        long totalPages) {
}
