package com.jexis.jexis_backend.cardholder.application.useCases;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.jexis.jexis_backend.cardholder.application.dto.EditCardHolderDto;
import com.jexis.jexis_backend.cardholder.domain.enums.CardHolderStatus;
import com.jexis.jexis_backend.stripe.application.useCases.UpdateStripeCardHolderUseCase;
import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.cardholder.domain.exceptions.CardHolderNotFoundException;
import com.jexis.jexis_backend.cardholder.infrastructure.CardHolderRepository;

@Service
public class DeleteCardHolderUseCase {
    private final CardHolderRepository repo;
    private final GetCardHolderUseCase getCardHolderUseCase;
    private final UpdateStripeCardHolderUseCase updateStripeCardHolderUseCase;

    public DeleteCardHolderUseCase(CardHolderRepository repo, GetCardHolderUseCase getCardHolderUseCase, UpdateStripeCardHolderUseCase updateStripeCardHolderUseCase) {
        this.repo = repo;
        this.getCardHolderUseCase = getCardHolderUseCase;
        this.updateStripeCardHolderUseCase = updateStripeCardHolderUseCase;
    }

    public void execute(UUID cardHolderId) {
        CardHolder cardHolder = getCardHolderUseCase.execute(cardHolderId);

        EditCardHolderDto dto = EditCardHolderDto.withStatus(CardHolderStatus.inactive);
        updateStripeCardHolderUseCase.execute(cardHolder.getAccount().getConnectAccountId(), cardHolder.getStripeCardHolderId(), dto);

        cardHolder.setIsDeleted(true);
        cardHolder.setDeletedAt(LocalDateTime.now());

        repo.save(cardHolder);
    }
}
