package com.jexis.jexis_backend.transaction.application.useCases;

import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.dispute.infrastructure.DisputeRepository;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.infrastructure.TransactionRepository;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateCardTransactionUseCase {
    private final TransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;
    private final AsyncLogger asyncLogger;
    private final SyncBalanceUseCase syncBalanceUseCase;

    public void execute(com.stripe.model.issuing.Transaction issuingTransaction) {
        Optional<Transaction> transaction = transactionRepository.findByStripeObjectId(issuingTransaction.getId());
        Optional<Dispute> dispute = disputeRepository.findByStripeDisputeId(issuingTransaction.getDispute());

        if (transaction.isPresent()) {
            syncBalanceUseCase.execute(
                    transaction.get().getWallet().getAccount().getConnectAccountId(),
                    transaction.get().getWallet().getStripeFinancialAccountId()
            );

            transaction.get().setAmount(issuingTransaction.getAmount());
            transaction.get().setCurrency(issuingTransaction.getCurrency());
            // Older or incomplete events must not erase an already known merchant amount.
            if (issuingTransaction.getMerchantAmount() != null && issuingTransaction.getMerchantCurrency() != null) {
                transaction.get().setMerchantAmount(issuingTransaction.getMerchantAmount());
                transaction.get().setMerchantCurrency(issuingTransaction.getMerchantCurrency());
            }
            transaction.get().setMerchantName(issuingTransaction.getMerchantData().getName());
            transaction.get().setMerchantCategory(issuingTransaction.getMerchantData().getCategory());
            transaction.get().setMerchantCountry(issuingTransaction.getMerchantData().getCountry());
            transaction.get().setMerchantCity(issuingTransaction.getMerchantData().getCity());
            transaction.get().setDispute(dispute.orElse(null));

            transactionRepository.save(transaction.get());
        } else {
            asyncLogger.info("STRIPE_WEBHOOK", "Issuing transaction update received for unknown transaction: " + issuingTransaction.getId());
        }
    }
}
