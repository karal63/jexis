package com.jexis.jexis_backend.member.application.dto;

import java.util.List;

public record MemberPageAdminResponseDto(
        List<MemberAdminResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages) {

}
