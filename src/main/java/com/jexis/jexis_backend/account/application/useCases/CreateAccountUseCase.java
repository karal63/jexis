package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.subscription.application.useCases.CreateFreeSubscriptionUseCase;
import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.exceptions.UserNotActivatedException;
import com.stripe.param.AccountLinkCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import org.springframework.transaction.annotation.Transactional;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.stripe.application.useCases.CreateConnectUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.CreateLinkUseCase;
import com.jexis.jexis_backend.account.application.dto.AccountResponseDto;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.stripe.model.AccountLink;

/**
 * CreateAccountUseCase
 * This service class implements the use case for creating a new account.
 * It contains only the business logic related to account creation, such as
 * validating input data and interacting with the repository to persist the new
 * account.
 * Author: Leo
 */
@Service
@RequiredArgsConstructor
public class CreateAccountUseCase {
    private final AccountRepository repo;
    private final AsyncLogger logger;
    private final CreateConnectUseCase createConnectUseCase;
    private final CreateLinkUseCase createLinkUseCase;
    private final DtoHelper dtoHelper;
    private final MemberRepository memberRepository;
    private final GetUserUseCase getUserUseCase;
    private final CreateFreeSubscriptionUseCase createFreeSubscriptionUseCase;

    /**
     * Handles account creation.
     * Creates Stripe account
     *
     * @param authUser passed by controller payload containing account creation data
     * @return the newly created account
     */
    @Transactional
    public AccountResponseDto execute(AuthUser authUser) {
        User user = getUserUseCase.execute(authUser.id());

        if (!user.getIsActivated()) throw new UserNotActivatedException();

        com.stripe.model.Account connectAccount = createConnectUseCase.execute(user.getEmail());
        AccountLink link = createLinkUseCase.execute(connectAccount.getId(), AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING);

        Account account = new Account(
                user.getEmail(),
                connectAccount.getId(),
                link.getUrl(),
                user);

        Account saved = repo.save(account);
        logger.info("ACCOUNT", "Account created successfully: " + saved.getFirstName() + " " + saved.getLastName());

        initializeOwner(saved, user);
        createFreeSubscriptionUseCase.execute(saved);
        logger.info("MEMBER", "Initial member created successfully");
        return dtoHelper.toAccountDto(saved);
    }

    // Only account creation can bootstrap its first member before subscribing.
    private void initializeOwner(Account account, User owner) {
        memberRepository.save(new Member(account, owner, Role.OWNER));
    }
}
