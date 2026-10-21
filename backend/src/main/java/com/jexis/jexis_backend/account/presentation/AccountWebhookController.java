package com.jexis.jexis_backend.account.presentation;

import com.jexis.jexis_backend.account.application.useCases.EditAccountUseCase;
import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.externalAccount.application.useCases.DeleteExternalAccountUseCase;
import com.jexis.jexis_backend.externalAccount.application.useCases.SynchronizeExternalAccountUseCase;
import com.stripe.model.BankAccount;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/accounts")
@RequiredArgsConstructor
public class AccountWebhookController {
    @Value("${stripe.webhook.secret.account}")
    private String webhookSecret;
    private final EditAccountUseCase editAccountUseCase;
    private final AsyncLogger logger;
    private final SynchronizeExternalAccountUseCase synchronizeExternalAccountUseCase;
    private final DeleteExternalAccountUseCase deleteExternalAccountUseCase;

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
            case "account.updated":
                editAccountUseCase.execute(event.getAccount());
                break;
            case "account.external_account.created":
                logger.info("STRIPE_WEBHOOK", "account.external_account.created webhook arrived");
                BankAccount createData = (BankAccount) event.getDataObjectDeserializer().getObject().orElseThrow();
                synchronizeExternalAccountUseCase.execute(createData);
                break;
            case "account.external_account.deleted":
                logger.info("STRIPE_WEBHOOK", "account.external_account.deleted webhook arrived");
                BankAccount deletedData = (BankAccount) event.getDataObjectDeserializer().getObject().orElseThrow();
                deleteExternalAccountUseCase.execute(deletedData.getId());
                break;
            case "account.external_account.updated":
                logger.info("STRIPE_WEBHOOK", "account.external_account.updated webhook arrived");
                BankAccount updateData = (BankAccount) event.getDataObjectDeserializer().getObject().orElseThrow();
                synchronizeExternalAccountUseCase.execute(updateData);
                break;

        }

        return ResponseEntity.ok("success");
    }
}
