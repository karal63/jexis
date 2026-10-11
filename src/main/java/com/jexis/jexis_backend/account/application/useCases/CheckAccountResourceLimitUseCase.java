package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.account.application.dto.AccountResourceLimitDto;
import com.jexis.jexis_backend.account.domain.enums.AccountResource;
import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import com.jexis.jexis_backend.cardholder.infrastructure.CardHolderRepository;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;
import com.jexis.jexis_backend.subscription.application.useCases.GetEffectiveSubscriptionEntitlementsUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckAccountResourceLimitUseCase {
    private final GetAccountUseCase getAccountUseCase;
    private final SubscriptionRepository subscriptionRepository;
    private final GetEffectiveSubscriptionEntitlementsUseCase getEntitlements;
    private final CardRepository cardRepository;
    private final MemberRepository memberRepository;
    private final CardHolderRepository cardHolderRepository;
    private final WalletRepository walletRepository;

    public AccountResourceLimitDto execute(UUID accountId, AccountResource resource, long additionalQuantity) {
        if (additionalQuantity < 0) {
            throw new ResourceLimitException(400, "INVALID_QUANTITY", "Quantity must be a nonnegative integer");
        }
        getAccountUseCase.execute(accountId);
        long used = switch (resource) {
            case CARDS -> cardRepository.countResourceUsage(accountId, List.of(CardStatus.active, CardStatus.inactive));
            case MEMBERS -> memberRepository.countByAccountId(accountId);
            case CARDHOLDERS -> cardHolderRepository.countByAccountIdAndIsDeletedFalse(accountId);
            case WALLETS -> walletRepository.countByAccountIdAndIsDeletedFalse(accountId);
        };
        List<Subscription> subscriptions = eligibleSubscriptions(accountId);
        if (subscriptions.isEmpty()) return denied(used, additionalQuantity, "NO_ELIGIBLE_SUBSCRIPTION");
        if (subscriptions.size() != 1) return denied(used, additionalQuantity, "AMBIGUOUS_SUBSCRIPTION");

        var entitlement = getEntitlements.execute(subscriptions.get(0).getId()).stream()
                .filter(item -> resource.entitlementKey().equals(item.key())).findFirst();
        if (entitlement.isEmpty()) return denied(used, additionalQuantity, "LIMIT_NOT_CONFIGURED");
        String value = entitlement.get().value();

        // The entitlement type is a business category; the resource key defines this value as a limit.
        if (value == null || !value.matches("[0-9]+")) {
            return denied(used, additionalQuantity, "INVALID_LIMIT_CONFIGURATION");
        }
        long limit;
        try {
            limit = Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return denied(used, additionalQuantity, "INVALID_LIMIT_CONFIGURATION");
        }
        boolean allowed = used <= limit && additionalQuantity <= limit - used;
        return new AccountResourceLimitDto(allowed, used, limit, Math.max(0, limit - used),
                additionalQuantity, allowed ? null : "LIMIT_EXCEEDED");
    }

    public void requireCapacity(UUID accountId, AccountResource resource, long additionalQuantity) {
        var result = execute(accountId, resource, additionalQuantity);
        if (!result.allowed())
            throw new ResourceLimitException(403, result.reason(), "Account resource limit check denied");
    }

    public void requireEligibleSubscription(UUID accountId) {
        var subscriptions = eligibleSubscriptions(accountId);
        if (subscriptions.size() != 1) {
            throw new ResourceLimitException(403, subscriptions.isEmpty()
                    ? "NO_ELIGIBLE_SUBSCRIPTION" : "AMBIGUOUS_SUBSCRIPTION", "An eligible subscription is required");
        }
    }

    private List<Subscription> eligibleSubscriptions(UUID accountId) {
        return subscriptionRepository.findEligibleForResourceLimits(accountId, LocalDateTime.now(ZoneOffset.UTC),
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING));
    }

    private AccountResourceLimitDto denied(long used, long quantity, String reason) {
        return new AccountResourceLimitDto(false, used, null, 0, quantity, reason);
    }
}
