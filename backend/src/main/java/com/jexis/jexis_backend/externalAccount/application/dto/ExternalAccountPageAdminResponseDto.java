package com.jexis.jexis_backend.externalAccount.application.dto;

import java.util.List;

public record ExternalAccountPageAdminResponseDto(
        List<ExternalAccountAdminResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages) {

}
