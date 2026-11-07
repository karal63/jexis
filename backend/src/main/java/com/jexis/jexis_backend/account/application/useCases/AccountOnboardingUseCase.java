package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.account.application.dto.AccountOnboardingDto;
import com.jexis.jexis_backend.account.domain.enums.OnboardingStatus;
import com.jexis.jexis_backend.stripe.application.useCases.CreateLinkUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.RetrieveAccountByIdUseCase;
import com.stripe.model.Account;
import com.stripe.param.AccountLinkCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountOnboardingUseCase {
    private final GetAccountUseCase getAccountUseCase;
    private final RetrieveAccountByIdUseCase retrieveAccountByIdUseCase;
    private final CreateLinkUseCase createLinkUseCase;

    public OnboardingStatus getStatus(UUID accountId) {
        return resolveStatus(retrieveAccountByIdUseCase.execute(
                getAccountUseCase.execute(accountId).getConnectAccountId()));
    }

    public AccountOnboardingDto createLink(UUID accountId) {
        String connectAccountId = getAccountUseCase.execute(accountId).getConnectAccountId();
        OnboardingStatus status = resolveStatus(retrieveAccountByIdUseCase.execute(connectAccountId));
        String url = status.requiresOnboarding()
                ? createLinkUseCase.execute(connectAccountId, accountId, AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING).getUrl()
                : null;
        return new AccountOnboardingDto(status, true, url);
    }

    static OnboardingStatus resolveStatus(Account account) {
        Account.Requirements requirements = account.getRequirements();
        String disabledReason = requirements == null ? null : requirements.getDisabledReason();

        if (Boolean.TRUE.equals(account.getDeleted()) || isRestricted(disabledReason)) {
            return OnboardingStatus.RESTRICTED;
        }
        if (!Boolean.TRUE.equals(account.getDetailsSubmitted())) {
            return OnboardingStatus.NOT_STARTED;
        }
        if (requirements != null && (hasEntries(requirements.getCurrentlyDue()) || hasEntries(requirements.getPastDue()))) {
            return OnboardingStatus.ACTION_REQUIRED;
        }
        Account.Capabilities capabilities = account.getCapabilities();
        if (disabledReason != null
                || (requirements != null && hasEntries(requirements.getPendingVerification()))
                || capabilities == null
                || !"active".equals(capabilities.getCardIssuing())
                || !"active".equals(capabilities.getTreasury())) {
            return OnboardingStatus.PENDING_VERIFICATION;
        }
        return OnboardingStatus.COMPLETED;
    }

    private static boolean hasEntries(List<String> fields) {
        return fields != null && !fields.isEmpty();
    }

    private static boolean isRestricted(String disabledReason) {
        return disabledReason != null
                && (disabledReason.startsWith("rejected.")
                    || "listed".equals(disabledReason)
                    || "other".equals(disabledReason)
                    || disabledReason.startsWith("platform_paused"));
    }
}
