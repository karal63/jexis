package com.jexis.jexis_backend.transaction.application.useCases;

import com.jexis.jexis_backend.authorization.application.useCases.GetAuthorizationByStripeIdUseCase;
import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.authorization.infrastructure.AuthorizationRepository;
import com.jexis.jexis_backend.card.application.useCases.GetCardByStripeIdUseCase;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.stripe.application.useCases.GetStripeDebitTransactionUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.GetStripeTransactionUseCase;
import com.jexis.jexis_backend.transaction.application.dto.CreateCardTransactionDto;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.infrastructure.TransactionRepository;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletByFAIdUseCase;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.stripe.model.treasury.ReceivedDebit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateCardTransactionUseCase {
    private final GetStripeTransactionUseCase getStripeTransactionUseCase;
    private final GetStripeDebitTransactionUseCase getStripeDebitTransactionUseCase;
    private final GetWalletByFAIdUseCase getWalletByFAIdUseCase;
    private final TransactionRepository repo;
    private final GetCardByStripeIdUseCase  getCardByStripeIdUseCase;
    private final GetAuthorizationByStripeIdUseCase  getAuthorizationByStripeIdUseCase;
    private final SyncBalanceUseCase syncBalanceUseCase;

    public void execute(CreateCardTransactionDto dto) {
        ReceivedDebit receivedDebit = getStripeDebitTransactionUseCase.execute(dto.accountId(), dto.debitTransactionId());
        com.stripe.model.treasury.Transaction treasuryTransaction = getStripeTransactionUseCase.execute(dto.accountId(), receivedDebit.getTransaction());
        syncBalanceUseCase.execute(dto.accountId(), treasuryTransaction.getFinancialAccount());
        
        Wallet wallet = getWalletByFAIdUseCase.execute(treasuryTransaction.getFinancialAccount());
        Card card = getCardByStripeIdUseCase.execute(dto.cardId());

        Optional<Authorization> authorization = Optional.empty();

        if (dto.authorizationId() != null) {
            authorization = Optional.of(getAuthorizationByStripeIdUseCase.execute(dto.authorizationId()));
        }

        Transaction transaction = new Transaction(
                wallet,
                treasuryTransaction.getId(),
                dto.stripeObjectId(),
                dto.type(),
                dto.amount(),
                dto.currency(),
                dto.status(),
                dto.direction()
        );

        transaction.setCard(card);
        transaction.setAuthorization(authorization.orElse(null));
        transaction.setMerchantAmount(dto.merchantAmount());
        transaction.setMerchantCurrency(dto.merchantCurrency());
        transaction.setMerchantName(dto.merchantName());
        transaction.setMerchantCategory(dto.merchantCategory());
        transaction.setMerchantCity(dto.merchantCity());
        transaction.setMerchantCountry(dto.merchantCountry());

        repo.save(transaction);
    }
}
