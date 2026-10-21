package com.jexis.jexis_backend.cardholder.application.dto;

import java.util.List;

public record CardHolderPageAdminResponseDto(
        List<CardHolderAdminResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages) {

}
