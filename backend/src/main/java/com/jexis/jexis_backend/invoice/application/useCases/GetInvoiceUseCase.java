package com.jexis.jexis_backend.invoice.application.useCases;

import com.jexis.jexis_backend.invoice.domain.entities.Invoice;
import com.jexis.jexis_backend.invoice.domain.exceptions.InvoiceNotFoundException;
import com.jexis.jexis_backend.invoice.infrastructure.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetInvoiceUseCase {
    private final InvoiceRepository invoiceRepository;

    public Invoice execute(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId).orElseThrow(InvoiceNotFoundException::new);
    }
}
