package com.jexis.jexis_backend.invoice.application.useCases;

import com.jexis.jexis_backend.invoice.domain.entities.Invoice;
import com.jexis.jexis_backend.stripe.application.useCases.invoice.PayStripeInvoiceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayInvoiceUseCase {
    private final PayStripeInvoiceUseCase payStripeInvoiceUseCase;
    private final GetInvoiceUseCase getInvoiceUseCase;

    public void execute(UUID invoiceId) {
        Invoice invoice = getInvoiceUseCase.execute(invoiceId);
        payStripeInvoiceUseCase.execute(invoice.getStripeInvoiceId());
    }
}
