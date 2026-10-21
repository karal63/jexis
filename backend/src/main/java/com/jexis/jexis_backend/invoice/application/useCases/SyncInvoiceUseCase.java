package com.jexis.jexis_backend.invoice.application.useCases;

import com.jexis.jexis_backend.invoice.domain.entities.Invoice;
import com.jexis.jexis_backend.invoice.domain.enums.InvoiceStatus;
import com.jexis.jexis_backend.invoice.infrastructure.InvoiceRepository;
import com.jexis.jexis_backend.stripe.application.useCases.subscription.GetStripeSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.application.useCases.GetSubscriptionByStripeIdUseCase;
import com.jexis.jexis_backend.subscription.application.useCases.SyncSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SyncInvoiceUseCase {

    private final GetSubscriptionByStripeIdUseCase getSubscriptionByStripeIdUseCase;
    private final InvoiceRepository invoiceRepository;
    private final SyncSubscriptionUseCase syncSubscriptionUseCase;
    private final GetStripeSubscriptionUseCase getStripeSubscriptionUseCase;

    @Transactional
    public void execute(com.stripe.model.Invoice stripeInvoice) {
        Invoice invoice = invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId()).orElseGet(Invoice::new);

        String subscriptionId = stripeInvoice.getLines().getData().get(0).getParent().getSubscriptionItemDetails().getSubscription();
        com.stripe.model.Subscription stripeSubscription = getStripeSubscriptionUseCase.execute(subscriptionId);

        Subscription subscription = syncSubscriptionUseCase.execute(stripeSubscription);

        invoice.setSubscription(subscription);
        invoice.setStripeInvoiceId(stripeInvoice.getId());
        invoice.setAmountPaid(stripeInvoice.getAmountPaid());
        invoice.setCurrency(stripeInvoice.getCurrency());
        invoice.setStatus(mapInvoiceStatus(stripeInvoice.getStatus()));
        invoice.setInvoicePdf(stripeInvoice.getInvoicePdf());
        invoice.setHostedInvoiceUrl(stripeInvoice.getHostedInvoiceUrl());

        invoiceRepository.save(invoice);
    }

    private InvoiceStatus mapInvoiceStatus(String status) {
        return switch (status.toLowerCase()) {
            case "open" -> InvoiceStatus.OPEN;
            case "paid" -> InvoiceStatus.PAID;
            default -> throw new IllegalArgumentException("Unknown invoice status: " + status);
        };
    }
}
