package com.jexis.jexis_backend.invoice.application.useCases;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.invoice.domain.entities.Invoice;
import com.jexis.jexis_backend.invoice.infrastructure.InvoiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAllInvoicesUseCase {
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public Page<Invoice> execute(int page, int pageSize) {
        return invoiceRepository.findAll(PageRequest.of(Math.max(0, page - 1), pageSize));
    }
}
