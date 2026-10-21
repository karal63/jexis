package com.jexis.jexis_backend.dispute.application.dto;

import java.util.List;

public record DisputePageAdminResponseDto(
        List<DisputeAdminResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages) {

}
