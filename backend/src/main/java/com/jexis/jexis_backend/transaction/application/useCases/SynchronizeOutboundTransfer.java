package com.jexis.jexis_backend.transaction.application.useCases;

import com.jexis.jexis_backend.stripe.application.useCases.GetStripeTransactionUseCase;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.enums.PaymentMethod;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import com.jexis.jexis_backend.transaction.infrastructure.TransactionRepository;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletByFAIdUseCase;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.stripe.model.ExpandableField;
import com.stripe.model.treasury.FinancialAccount;
import com.stripe.model.treasury.OutboundTransfer;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SynchronizeOutboundTransfer {
    private final EntityManager entityManager;
    private final TransactionRepository transactionRepository;
    private final GetStripeTransactionUseCase getStripeTransactionUseCase;
    private final GetWalletByFAIdUseCase getWalletByFAIdUseCase;
    private final SyncBalanceUseCase syncBalanceUseCase;

    @Transactional
    public void synchronize(String accountId, OutboundTransfer transfer, TransactionStatus status) {
        // 1. Lock this specific Stripe object
        entityManager.createNativeQuery(
                        "SELECT pg_advisory_xact_lock(hashtext(?1))")
                .setParameter(1, transfer.getId())
                .getSingleResult();

        // 2. Find existing transaction or create new one
        Optional<Transaction> existingTransaction = transactionRepository
                .findByStripeObjectId(transfer.getId());

        if (existingTransaction.isPresent()) {
            updateStatus(existingTransaction.get(), status);
            transactionRepository.save(existingTransaction.get());
        } else {
            com.stripe.model.treasury.Transaction treasuryTransaction = getStripeTransactionUseCase.execute(accountId, transfer.getTransaction());
            syncBalanceUseCase.execute(accountId, treasuryTransaction.getFinancialAccount());

            Wallet wallet = getWalletByFAIdUseCase.execute(transfer.getFinancialAccount());

            Transaction transaction = new Transaction(
                    wallet,
                    treasuryTransaction.getId(),
                    transfer.getId(),
                    TransactionType.INBOUND_TRANSFER,
                    treasuryTransaction.getAmount(),
                    treasuryTransaction.getCurrency(),
                    TransactionStatus.COMPLETED,
                    TransactionDirection.CREDIT
            );

            transaction.setBankName(transfer.getDestinationPaymentMethodDetails().getUsBankAccount().getBankName());
            transaction.setPaymentMethod(PaymentMethod.valueOf(transfer.getDestinationPaymentMethodDetails().getUsBankAccount().getNetwork().toUpperCase()));
            transaction.setBankAccountLast4(transfer.getDestinationPaymentMethodDetails().getUsBankAccount().getLast4());
            transaction.setRoutingNumber(transfer.getDestinationPaymentMethodDetails().getUsBankAccount().getRoutingNumber());

            if (transfer.getExpectedArrivalDate() != null) {
                transaction.setExpectedArrivalDate(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(transfer.getExpectedArrivalDate()),
                                ZoneId.systemDefault()
                        )
                );
            }

            transactionRepository.save(transaction);
        }
    }

    private void updateStatus(Transaction transaction, TransactionStatus incomingStatus) {

        TransactionStatus currentStatus = transaction.getStatus();

        if (currentStatus == null ||
                incomingStatus.getPriority() >= currentStatus.getPriority()) {

            transaction.setStatus(incomingStatus);
        }
    }
}
