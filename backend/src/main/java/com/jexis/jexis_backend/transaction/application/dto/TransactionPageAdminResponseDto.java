package com.jexis.jexis_backend.transaction.application.dto;

import java.util.List;

public record TransactionPageAdminResponseDto(
        List<TransactionAdminResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages) {

}
