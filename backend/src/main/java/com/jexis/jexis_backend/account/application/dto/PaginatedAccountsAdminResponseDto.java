package com.jexis.jexis_backend.account.application.dto;

import java.util.List;

public record PaginatedAccountsAdminResponseDto(
        List<AccountAdminResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages) {

}
