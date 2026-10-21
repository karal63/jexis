package com.jexis.jexis_backend.card.application.dto;

import java.util.List;

public record CardPageAdminResponseDto(
        List<CardAdminResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages) {

}
