package com.jexis.jexis_backend.member.application.dto;

import java.util.List;

public record MemberPageResponseDto(
        List<MemberResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        long totalPages) {
}
