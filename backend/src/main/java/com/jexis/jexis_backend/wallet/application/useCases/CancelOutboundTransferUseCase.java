package com.jexis.jexis_backend.wallet.application.useCases;

import com.jexis.jexis_backend.stripe.application.useCases.StripeCancelOutboundTransferUseCase;
import com.jexis.jexis_backend.transaction.application.useCases.GetTransactionUseCase;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.exceptions.WrongTransactionStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CancelOutboundTransferUseCase {
    private final GetTransactionUseCase getTransactionUseCase;
    private final StripeCancelOutboundTransferUseCase stripeCancelOutboundTransferUseCase;

    public void execute(UUID transactionId) {
        Transaction transaction = getTransactionUseCase.execute(transactionId);

        if (transaction.getStatus() != TransactionStatus.PROCESSING) throw new WrongTransactionStatusException(transaction.getStatus());

        String stripeAccountId = transaction.getWallet().getAccount().getConnectAccountId();
        stripeCancelOutboundTransferUseCase.execute(stripeAccountId, transaction.getStripeObjectId());
    }
}
