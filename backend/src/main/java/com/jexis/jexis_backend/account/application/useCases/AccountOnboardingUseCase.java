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
    private static final String EXTERNAL_ACCOUNT = "external_account";

    private final GetAccountUseCase getAccountUseCase;
    private final RetrieveAccountByIdUseCase retrieveAccountByIdUseCase;
    private final CreateLinkUseCase createLinkUseCase;

    public AccountOnboardingDto getStatus(UUID accountId, boolean canOnboard) {
        Account account = retrieveAccountByIdUseCase.execute(
                getAccountUseCase.execute(accountId).getConnectAccountId());
        return new AccountOnboardingDto(resolveStatus(account), canOnboard, null, externalAccountRequired(account));
    }

    public AccountOnboardingDto createLink(UUID accountId) {
        String connectAccountId = getAccountUseCase.execute(accountId).getConnectAccountId();
        Account account = retrieveAccountByIdUseCase.execute(connectAccountId);
        OnboardingStatus status = resolveStatus(account);
        String url = status.requiresOnboarding()
                ? createLinkUseCase.execute(connectAccountId, accountId, AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING).getUrl()
                : null;
        return new AccountOnboardingDto(status, true, url, externalAccountRequired(account));
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
        if (requirements != null && (hasOnboardingFields(requirements.getCurrentlyDue())
                || hasOnboardingFields(requirements.getPastDue()))) {
            return OnboardingStatus.ACTION_REQUIRED;
        }
        Account.Capabilities capabilities = account.getCapabilities();
        boolean onlyExternalAccountOverdue = "requirements.past_due".equals(disabledReason)
                && containsExternalAccount(requirements.getPastDue());
        if ((disabledReason != null && !onlyExternalAccountOverdue)
                || (requirements != null && hasEntries(requirements.getPendingVerification()))
                || capabilities == null
                || !"active".equals(capabilities.getCardIssuing())
                || !"active".equals(capabilities.getTreasury())) {
            return OnboardingStatus.PENDING_VERIFICATION;
        }
        return OnboardingStatus.COMPLETED;
    }

    private static boolean externalAccountRequired(Account account) {
        Account.Requirements requirements = account.getRequirements();
        return requirements != null && (containsExternalAccount(requirements.getCurrentlyDue())
                || containsExternalAccount(requirements.getPastDue()));
    }

    private static boolean containsExternalAccount(List<String> fields) {
        return fields != null && fields.contains(EXTERNAL_ACCOUNT);
    }

    private static boolean hasOnboardingFields(List<String> fields) {
        return fields != null && fields.stream().anyMatch(field -> !EXTERNAL_ACCOUNT.equals(field));
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
