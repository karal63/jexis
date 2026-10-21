package com.jexis.jexis_backend.invoice.application.useCases;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.invoice.domain.entities.Invoice;
import com.jexis.jexis_backend.invoice.infrastructure.InvoiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetSubscriptionInvoicesUseCase {
    private final InvoiceRepository invoiceRepository;

    @Transactional(readOnly = true)
    public Page<Invoice> execute(UUID subscriptionId, int page, int pageSize) {
        return invoiceRepository.findBySubscriptionId(
                subscriptionId,
                PageRequest.of(Math.max(0, page - 1), pageSize)
        );
    }
}
