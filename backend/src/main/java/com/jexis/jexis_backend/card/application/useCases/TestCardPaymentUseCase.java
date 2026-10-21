package com.jexis.jexis_backend.card.application.useCases;

import com.jexis.jexis_backend.card.application.dto.TestCardPaymentDto;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.stripe.application.useCases.TestStripeCardPaymentUseCase;
import org.springframework.stereotype.Service;

@Service
public class TestCardPaymentUseCase {
    private final TestStripeCardPaymentUseCase testStripeCardPaymentUseCase;
    private final GetCardUseCase getCardUseCase;

    public TestCardPaymentUseCase(TestStripeCardPaymentUseCase testStripeCardPaymentUseCase, GetCardUseCase getCardUseCase) {
        this.testStripeCardPaymentUseCase = testStripeCardPaymentUseCase;
        this.getCardUseCase = getCardUseCase;
    }

    public void execute(TestCardPaymentDto body) {
        Card card = getCardUseCase.execute(body.cardId());
        String stripeAccountId = card.getCardHolder().getAccount().getConnectAccountId();

        testStripeCardPaymentUseCase.execute(card.getStripeCardId(), body.amount(), body.currency(), stripeAccountId);
    }
}
