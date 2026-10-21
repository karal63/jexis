package com.jexis.jexis_backend.card.presentation;

import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.transaction.application.dto.CreateCardTransactionDto;
import com.jexis.jexis_backend.transaction.application.useCases.CreateCardTransactionUseCase;
import com.jexis.jexis_backend.transaction.application.useCases.UpdateCardTransactionUseCase;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.issuing.Dispute;
import com.stripe.model.issuing.Transaction;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/cards")
@RequiredArgsConstructor
public class CardWebhookController {
    @Value("${stripe.webhook.secret.card}")
    private String webhookSecret;
    private final AsyncLogger logger;
    private final CreateCardTransactionUseCase createCardTransactionUseCase;
    private final UpdateCardTransactionUseCase updateCardTransactionUseCase;

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
            case "issuing_transaction.created":
                Transaction transaction = (Transaction) event.getDataObjectDeserializer()
                        .getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));


                CreateCardTransactionDto dto = new CreateCardTransactionDto(
                        event.getAccount(),
                        transaction.getTreasury().getReceivedDebit(),
                        transaction.getId(),
                        TransactionType.CARD_TRANSACTION,
                        transaction.getAmount(),
                        transaction.getCurrency(),
                        transaction.getMerchantAmount(),
                        transaction.getMerchantCurrency(),
                        TransactionStatus.COMPLETED,
                        TransactionDirection.DEBIT,
                        transaction.getCard(),
                        transaction.getAuthorization(),
                        transaction.getMerchantData().getName(),
                        transaction.getMerchantData().getCategory(),
                        transaction.getMerchantData().getCity(),
                        transaction.getMerchantData().getCountry()
                );

                createCardTransactionUseCase.execute(dto);
                logger.info("STRIPE", "Issuing transaction created");
                break;
            case "issuing_transaction.updated":
                logger.info("STRIPE_WEBHOOK", "Issuing transaction update webhook arrived");
                Transaction updatedTransaction = (Transaction) event.getDataObjectDeserializer()
                        .getObject().orElseThrow(() -> new IllegalStateException("Unable to deserialize object"));

                updateCardTransactionUseCase.execute(updatedTransaction);

                break;
        }
        return ResponseEntity.ok("success");
    }
}
