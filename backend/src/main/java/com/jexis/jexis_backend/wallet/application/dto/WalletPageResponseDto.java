package com.jexis.jexis_backend.wallet.application.dto;

import java.util.List;

public record WalletPageResponseDto(
        List<WalletResponseDto> items,
        int page,
        int pageSize,
        long totalItems,
        long totalPages) {
}
