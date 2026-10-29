package com.jexis.jexis_backend.wallet.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;
import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import com.jexis.jexis_backend.wallet.application.dto.CreateOutboundTransferDto;
import com.jexis.jexis_backend.wallet.domain.entities.*;
import com.jexis.jexis_backend.wallet.infrastructure.OutboundTransferCommandRepository;
import java.time.Instant;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
@RequiredArgsConstructor
public class OutboundTransferCommands {
    private final OutboundTransferCommandRepository repo;
    private final LockAccountUseCase lockAccount;

    // Commit the identity BEFORE contacting Stripe, including when its response is lost.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public OutboundTransferCommand prepare(Wallet wallet, ExternalAccount destination,
            CreateOutboundTransferDto body, UUID key) {
        lockAccount.execute(wallet.getAccount().getId());
        var existing = repo.findByWalletIdAndRequestKey(wallet.getId(), key);
        if (existing.isPresent()) {
            var command = existing.get();
            if (!command.getAmount().equals(body.amount())
                    || !command.getCurrency().equalsIgnoreCase(body.currency())
                    || !command.getDescription().equals(body.description())
                    || !command.getExternalAccountId().equals(body.externalAccountId())) {
                throw new ResourceLimitException(409, "IDEMPOTENCY_KEY_REUSED",
                        "This key was already used with different transfer details");
            }
            // Stripe may prune keys after 24 hours. Never resend an unresolved old command.
            if (command.getStripeTransferId() == null
                    && !command.getCreatedAt().plus(Duration.ofHours(23)).isAfter(Instant.now())) {
                throw new ResourceLimitException(409, "TRANSFER_RECONCILIATION_REQUIRED",
                        "Transfer outcome is unknown; reconcile with Stripe before sending again");
            }
            return command;
        }
        var command = new OutboundTransferCommand();
        command.setId(UUID.randomUUID());
        command.setWalletId(wallet.getId());
        command.setRequestKey(key);
        command.setExternalAccountId(body.externalAccountId());
        command.setAmount(body.amount());
        command.setCurrency(body.currency().toLowerCase(java.util.Locale.ROOT));
        command.setDescription(body.description());
        command.setStripeAccountId(wallet.getAccount().getConnectAccountId());
        command.setFinancialAccountId(wallet.getStripeFinancialAccountId());
        command.setDestinationId(destination.getStripeExternalAccountId());
        command.setCreatedAt(Instant.now());
        return repo.save(command);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(UUID id, String stripeTransferId) {
        var command = repo.findById(id).orElseThrow();
        command.setStripeTransferId(stripeTransferId);
    }
}
