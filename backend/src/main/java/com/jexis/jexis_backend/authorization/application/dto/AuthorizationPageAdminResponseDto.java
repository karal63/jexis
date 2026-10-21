package com.jexis.jexis_backend.authorization.application.dto;

import java.util.List;

public record AuthorizationPageAdminResponseDto(
        List<AuthorizationAdminResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages) {

}
