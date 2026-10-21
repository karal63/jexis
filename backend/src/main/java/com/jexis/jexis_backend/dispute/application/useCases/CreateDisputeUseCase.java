package com.jexis.jexis_backend.dispute.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.dispute.application.dto.CreateDisputeDto;
import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus;
import com.jexis.jexis_backend.dispute.domain.exceptions.DisputeExistsException;
import com.jexis.jexis_backend.dispute.infrastructure.DisputeRepository;
import com.jexis.jexis_backend.stripe.application.useCases.createStripeDispute.CreateStripeDisputeUseCase;
import com.jexis.jexis_backend.transaction.application.useCases.GetTransactionUseCase;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateDisputeUseCase {
    private final CreateStripeDisputeUseCase createStripeDisputeUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final GetTransactionUseCase getTransactionUseCase;
    private final GetWalletUseCase getWalletUseCase;
    private final DisputeRepository repo;

    public Dispute execute(CreateDisputeDto body) {
        Account account = getAccountUseCase.execute(body.accountId());
        Transaction transaction = getTransactionUseCase.execute(body.transactionId());

        if (transaction.getDispute() != null) {
            throw new DisputeExistsException();
        }

        com.stripe.model.issuing.Dispute stripeDispute = createStripeDisputeUseCase.execute(body, account.getConnectAccountId(), transaction.getStripeObjectId());

        Wallet wallet = getWalletUseCase.execute(transaction.getWallet().getId());

        Dispute dispute = new Dispute(
                stripeDispute.getId(),
                transaction,
                wallet,
                body.amount(),
                transaction.getCurrency(),
                DisputeStatus.NEEDS_RESPONSE,
                body.disputeEvidence().reason()
        );

        return repo.save(dispute);
    }
}
