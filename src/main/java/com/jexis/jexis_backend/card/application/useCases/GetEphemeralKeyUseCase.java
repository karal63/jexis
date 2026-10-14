package com.jexis.jexis_backend.card.application.useCases;

import com.jexis.jexis_backend.card.application.dto.CreateEphemeralKeyDto;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.stripe.application.useCases.GetStripeEphemeralKeyUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetEphemeralKeyUseCase {
    private final GetStripeEphemeralKeyUseCase getStripeEphemeralKeyUseCase;
    private final GetCardUseCase getCardUseCase;

    public String execute(UUID cardId, CreateEphemeralKeyDto createEphemeralKeyDto) {
        Card card = getCardUseCase.execute(cardId);
        String stripeCardId = card.getStripeCardId();
        String connectAccountId = card.getCardHolder().getAccount().getConnectAccountId();

        return getStripeEphemeralKeyUseCase.execute(stripeCardId, connectAccountId, createEphemeralKeyDto);
    }
}
