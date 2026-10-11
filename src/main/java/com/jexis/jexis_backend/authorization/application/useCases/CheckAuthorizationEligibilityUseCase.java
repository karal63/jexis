package com.jexis.jexis_backend.authorization.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.CheckAccountResourceLimitUseCase;
import com.jexis.jexis_backend.card.application.useCases.GetCardByStripeIdUseCase;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckAuthorizationEligibilityUseCase {
    private final GetCardByStripeIdUseCase getCard;
    private final CheckAccountResourceLimitUseCase checkLimit;

    @Transactional(readOnly = true)
    public boolean execute(String stripeCardId) {
        if (stripeCardId == null || stripeCardId.isBlank()) return false;
        var card = getCard.execute(stripeCardId);
        if (card.getIsDeleted() || card.getStatus() != CardStatus.active || card.isReplacementPending()) {
            return false;
        }
        var accountId = card.getCardHolder().getAccount().getId();
        if (accountId == null || !accountId.equals(card.getTreasuryAccount().getAccount().getId())) {
            return false;
        }
        // Spending uses an existing resource; a downgrade over capacity must not block it.
        checkLimit.requireEligibleSubscription(accountId);
        return true;
    }
}
