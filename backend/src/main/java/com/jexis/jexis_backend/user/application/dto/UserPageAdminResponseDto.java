package com.jexis.jexis_backend.user.application.dto;

import java.util.List;

public record UserPageAdminResponseDto(
        List<UserAdminResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages) {

}
