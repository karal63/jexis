package com.jexis.jexis_backend.wallet.presentation;

import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.transaction.application.useCases.*;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.stripe.model.Event;
import com.stripe.model.treasury.OutboundTransfer;
import com.stripe.model.treasury.ReceivedCredit;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/wallets")
@RequiredArgsConstructor
public class WalletWebhookController {
    @Value("${stripe.webhook.secret.wallet}")
    private String webhookSecret;
    private final AsyncLogger logger;
    private final CreateBankTransactionUseCase createBankTransactionUseCase;
    private final UpdateOutboundTransferExpectedArrivalDateUseCase updateOutboundTransferExpectedArrivalDateUseCase;
    private final SynchronizeOutboundTransfer synchronizeOutboundTransfer;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        switch (event.getType()) {
            case "treasury.received_credit.created":
                logger.info("STRIPE_WEBHOOK", "Webhook received credit created");
                ReceivedCredit createdReceivedCredits = (ReceivedCredit) event
                        .getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                createBankTransactionUseCase.execute(event.getAccount(), createdReceivedCredits);
                logger.info("STRIPE_WEBHOOK", "Received treasury credit created");
                break;
            case "treasury.outbound_transfer.created":
                logger.info("STRIPE_WEBHOOK", "Webhook outbound transfer created");
                OutboundTransfer createdTransfer = (OutboundTransfer) event
                        .getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                synchronizeOutboundTransfer.synchronize(event.getAccount(), createdTransfer, TransactionStatus.PROCESSING);
                logger.info("STRIPE_WEBHOOK", "Outbound transfer created");
                break;
            case "treasury.outbound_transfer.canceled":
                logger.info("STRIPE_WEBHOOK", "Webhook outbound transfer canceled");
                OutboundTransfer canceledTransfer = (OutboundTransfer) event
                        .getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                synchronizeOutboundTransfer.synchronize(event.getAccount(), canceledTransfer, TransactionStatus.CANCELED);
                logger.info("STRIPE_WEBHOOK", "Outbound transfer canceled");
                break;
            case "treasury.outbound_transfer.failed":
                logger.info("STRIPE_WEBHOOK", "Webhook outbound transfer failed");
                OutboundTransfer failedTransfer = (OutboundTransfer) event
                        .getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                synchronizeOutboundTransfer.synchronize(event.getAccount(), failedTransfer, TransactionStatus.FAILED);
                logger.info("STRIPE_WEBHOOK", "Outbound transfer failed");
                break;
            case "treasury.outbound_transfer.expected_arrival_date_updated":
                logger.info("STRIPE_WEBHOOK", "Webhook expected arrival date updated");
                OutboundTransfer updatedDateTransfer = (OutboundTransfer) event
                        .getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                updateOutboundTransferExpectedArrivalDateUseCase.execute(updatedDateTransfer);
                logger.info("STRIPE_WEBHOOK", "Expected arrival date updated");
                break;
            case "treasury.outbound_transfer.posted":
                logger.info("STRIPE_WEBHOOK", "Webhook outbound transfer posted");
                OutboundTransfer postedTransfer = (OutboundTransfer) event
                        .getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                synchronizeOutboundTransfer.synchronize(event.getAccount(), postedTransfer, TransactionStatus.COMPLETED);
                logger.info("STRIPE_WEBHOOK", "Outbound transfer posted");
                break;
        }

        return ResponseEntity.ok("success");
    }
}