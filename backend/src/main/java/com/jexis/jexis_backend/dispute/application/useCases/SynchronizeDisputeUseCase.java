package com.jexis.jexis_backend.dispute.application.useCases;

import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.dispute.infrastructure.DisputeRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SynchronizeDisputeUseCase {
    private final EntityManager entityManager;
    private final DisputeRepository disputeRepository;
    private final AsyncLogger logger;

    @Transactional
    public void synchronize(com.stripe.model.issuing.Dispute stripeDispute) {
        entityManager.createNativeQuery("SELECT pg_advisory_xact_lock(hashtext(?1))")
                .setParameter(1, stripeDispute.getId())
                .getSingleResult();

        Optional<Dispute> existingDispute = disputeRepository.findByStripeDisputeId(stripeDispute.getId());

        if (existingDispute.isEmpty()) {
            logger.info("STRIPE_WEBHOOK", "Skipping dispute sync because the dispute does not exist yet: " + stripeDispute.getId());
            return;
        }

        Dispute dispute = existingDispute.get();

        dispute.setAmount(stripeDispute.getAmount());
        dispute.setCurrency(stripeDispute.getCurrency());

        updateStatus(dispute, stripeDispute);

        disputeRepository.save(dispute);
        logger.info("STRIPE_WEBHOOK", "Dispute synchronized: " + stripeDispute.getId());
    }

    private void updateStatus(Dispute dispute, com.stripe.model.issuing.Dispute stripeDispute) {
        com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus incomingStatus =
                com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus.fromStripeStatus(stripeDispute.getStatus());

        com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus currentStatus = dispute.getStatus();
        boolean shouldSetResolvedAt = incomingStatus.isTerminal()
                && (currentStatus == null || !currentStatus.isTerminal());

        if (currentStatus == null || incomingStatus.getPriority() >= currentStatus.getPriority()) {
            dispute.setStatus(incomingStatus);

            if (shouldSetResolvedAt) {
                dispute.setResolvedAt(LocalDateTime.now());
            }
        }
    }
}
