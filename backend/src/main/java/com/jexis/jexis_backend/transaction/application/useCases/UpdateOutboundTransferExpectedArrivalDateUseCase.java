package com.jexis.jexis_backend.transaction.application.useCases;

import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
    import com.jexis.jexis_backend.transaction.domain.exceptions.TransactionNotFoundException;
import com.jexis.jexis_backend.transaction.infrastructure.TransactionRepository;
import com.stripe.model.Event;
import com.stripe.model.treasury.OutboundTransfer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class UpdateOutboundTransferExpectedArrivalDateUseCase {
    private final TransactionRepository repo;

    public UpdateOutboundTransferExpectedArrivalDateUseCase(TransactionRepository repo) {
        this.repo = repo;
    }

    public void execute(OutboundTransfer transfer) {
        Transaction transaction = repo.findByStripeObjectId(transfer.getId())
                .orElseThrow(TransactionNotFoundException::new);

        if (transfer.getExpectedArrivalDate() != null) {
            transaction.setExpectedArrivalDate(
                    LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(transfer.getExpectedArrivalDate()),
                            ZoneId.systemDefault()
                    )
            );
            repo.save(transaction);
        }
    }
}
