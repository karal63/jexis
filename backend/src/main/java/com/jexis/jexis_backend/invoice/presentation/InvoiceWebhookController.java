package com.jexis.jexis_backend.invoice.presentation;

import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.invoice.application.useCases.SyncInvoiceUseCase;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/invoices")
@RequiredArgsConstructor
public class InvoiceWebhookController {
    private final SyncInvoiceUseCase syncInvoiceUseCase;
    @Value("${stripe.webhook.secret.invoice}")
    private String webhookSecret;
    private final AsyncLogger logger;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, this.webhookSecret);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("invalid signature");
        }

        switch (event.getType()) {
            case "invoice.paid":
                Invoice paidInvoice = (Invoice) event.getDataObjectDeserializer()
                        .getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));
                logger.info("STRIPE_WEBHOOK", "Received invoice.paid event for invoice ID: " + paidInvoice.getId());
                syncInvoiceUseCase.execute(paidInvoice);

                break;

            case "invoice.payment_failed":
                Invoice failedInvoice = (Invoice) event.getDataObjectDeserializer()
                        .getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));
                logger.info("STRIPE_WEBHOOK", "Received invoice.payment_failed event for invoice ID: " + failedInvoice.getId());
                syncInvoiceUseCase.execute(failedInvoice);

                break;
        }
        return ResponseEntity.ok("success");
    }
}
