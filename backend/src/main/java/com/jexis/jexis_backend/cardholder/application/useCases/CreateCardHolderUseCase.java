package com.jexis.jexis_backend.cardholder.application.useCases;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.account.application.useCases.CheckAccountResourceLimitUseCase;
import com.jexis.jexis_backend.account.domain.enums.AccountResource;

import com.jexis.jexis_backend.account.application.useCases.GetAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.card.domain.exceptions.UserIsNotMemberException;
import com.jexis.jexis_backend.cardholder.application.dto.CreateCardHolderDto;
import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.cardholder.domain.enums.CardHolderStatus;
import com.jexis.jexis_backend.cardholder.domain.exceptions.CardHolderExistsException;
import com.jexis.jexis_backend.cardholder.infrastructure.CardHolderRepository;
import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.member.application.useCases.CanAccessUseCase;
import com.jexis.jexis_backend.stripe.application.dto.CreateStripeHolderDto;
import com.jexis.jexis_backend.stripe.application.useCases.CreateStripeHolderUseCase;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;

import com.stripe.model.issuing.Cardholder;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class CreateCardHolderUseCase {
    private final CardHolderRepository repo;
    private final GetAccountUseCase getAccountUseCase;
    private final CreateStripeHolderUseCase createStripeHolder;
    private final GetUserUseCase getUserUseCase;
    private final CanAccessUseCase canAccessUseCase;
    private final LockAccountUseCase lockAccount;
    private final CheckAccountResourceLimitUseCase checkLimit;

    public CreateCardHolderUseCase(CardHolderRepository repo, GetAccountUseCase getAccountUseCase,
            CreateStripeHolderUseCase createStripeHolder, GetUserUseCase getUserUseCase, AsyncLogger logger,
            CanAccessUseCase canAccessUseCase, LockAccountUseCase lockAccount,
            CheckAccountResourceLimitUseCase checkLimit) {
        this.lockAccount = lockAccount;
        this.checkLimit = checkLimit;
        this.repo = repo;
        this.getAccountUseCase = getAccountUseCase;
        this.createStripeHolder = createStripeHolder;
        this.getUserUseCase = getUserUseCase;
        this.canAccessUseCase = canAccessUseCase;
    }

    @Transactional
    public CardHolder execute(CreateCardHolderDto body, HttpServletRequest request) {
        lockAccount.execute(body.accountId());
        if (!canAccessUseCase.execute(body.userId(), body.accountId())) {
            throw new UserIsNotMemberException();
        }

        Account account = getAccountUseCase.execute(body.accountId());
        User user = getUserUseCase.execute(body.userId());
        Optional<CardHolder> existingCardHolder = repo.findByUserEmailAndAccountId(user.getEmail(), account.getId());

        if (existingCardHolder.isPresent()) {
            throw new CardHolderExistsException(user.getEmail());
        }

        checkLimit.requireCapacity(account.getId(), AccountResource.CARDHOLDERS, 1);

        Cardholder stripeCardHolder = createStripeHolder.execute(request, new CreateStripeHolderDto(
                account.getConnectAccountId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                body.addressLine1(),
                body.city(),
                body.state(),
                body.country(),
                body.postalCode()));

        CardHolder cardHolder = new CardHolder(stripeCardHolder.getId(), account, user, user.getFirstName(),
                body.addressLine1(), body.city(), body.state(), body.country(),
                body.postalCode(), CardHolderStatus.valueOf(stripeCardHolder.getStatus()));

        return repo.save(cardHolder);
    }
}
