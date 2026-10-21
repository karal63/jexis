package com.jexis.jexis_backend.invoice.application.dto;

import com.jexis.jexis_backend.invoice.domain.entities.Invoice;

import java.util.List;

public record InvoicePageResponseDto(
        List<Invoice> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages
) {
}
