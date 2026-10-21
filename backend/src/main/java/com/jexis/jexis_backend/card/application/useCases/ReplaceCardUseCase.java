package com.jexis.jexis_backend.card.application.useCases;

import com.jexis.jexis_backend.card.application.dto.ReplaceCardDto;
import com.jexis.jexis_backend.card.domain.entities.Card;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReplaceCardUseCase {
    private final CardReplacementTransactions transactions;

    public Card execute(UUID cardId, ReplaceCardDto dto) {
        // Separate commits preserve cancellation when issuance fails.
        transactions.prepare(cardId, dto);
        return transactions.issue(cardId);
    }
}
