package com.jexis.jexis_backend.dispute.presentation;

import com.jexis.jexis_backend.dispute.application.useCases.SynchronizeDisputeUseCase;
import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.stripe.application.useCases.GetStripeDebitTransactionUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.GetStripeTransactionUseCase;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import com.stripe.model.Event;
import com.stripe.model.issuing.Dispute;
import com.stripe.model.issuing.Transaction;
import com.stripe.model.treasury.ReceivedDebit;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks/disputes")
public class DisputeWebhookController {
    private final SyncBalanceUseCase syncBalanceUseCase;
    private final GetStripeTransactionUseCase getStripeTransactionUseCase;
    private final GetStripeDebitTransactionUseCase getStripeDebitTransactionUseCase;
    @Value("${stripe.webhook.secret.dispute}")
    private String webhookSecret;
    private final AsyncLogger logger;
    private final SynchronizeDisputeUseCase synchronizeDisputeUseCase;

    @PostMapping
    public ResponseEntity<String> handleDisputeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, this.webhookSecret);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("invalid signature");
        }

        switch (event.getType()) {
            case "issuing_dispute.created":
                logger.info("STRIPE_WEBHOOK", "Created a new dispute");
                Dispute createdDispute = (Dispute) event.getDataObjectDeserializer().getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));
                synchronizeDisputeUseCase.synchronize(createdDispute);
                break;
            case "issuing_dispute.updated":
                logger.info("STRIPE_WEBHOOK", "Updated a dispute");
                Dispute updatedDispute = (Dispute) event.getDataObjectDeserializer().getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));
                synchronizeDisputeUseCase.synchronize(updatedDispute);
                break;
            case "issuing_dispute.closed":
                logger.info("STRIPE_WEBHOOK", "Closed a dispute");
                Dispute closedDispute = (Dispute) event.getDataObjectDeserializer().getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));
                synchronizeDisputeUseCase.synchronize(closedDispute);
                break;
            case "issuing_dispute.submitted":
                logger.info("STRIPE_WEBHOOK", "Submitted a dispute");
                Dispute submittedDispute = (Dispute) event.getDataObjectDeserializer().getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));
                synchronizeDisputeUseCase.synchronize(submittedDispute);
                break;
            case "issuing_dispute.funds_reinstated":
                logger.info("STRIPE_WEBHOOK", "Funds reinstated for a dispute");
                Dispute reinstatedDispute = (Dispute) event.getDataObjectDeserializer().getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));

                ReceivedDebit receivedDebit = getStripeDebitTransactionUseCase.execute(event.getAccount(), reinstatedDispute.getTreasury().getReceivedDebit());

                syncBalanceUseCase.execute(event.getAccount(), receivedDebit.getFinancialAccount());
                break;
        }
        return ResponseEntity.ok().body("Success");
    }

}
