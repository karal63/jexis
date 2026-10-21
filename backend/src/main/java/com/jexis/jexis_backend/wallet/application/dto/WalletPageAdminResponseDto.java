package com.jexis.jexis_backend.wallet.application.dto;

import java.util.List;

public record WalletPageAdminResponseDto(
        List<WalletAdminResponseDto> items,
        int page,
        int pageSize,
        long total,
        int pages) {

}
