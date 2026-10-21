package com.jexis.jexis_backend.card.application.useCases;

import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.exceptions.CardNotFoundException;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetLockedCardUseCase {
    private final LockAccountUseCase lockAccount;
    private final CardRepository repository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY)
    public Card execute(UUID cardId) {
        lockAccount.execute(repository.findAccountId(cardId).orElseThrow(CardNotFoundException::new));
        Card card = repository.findById(cardId).orElseThrow(CardNotFoundException::new);
        // Authorization may have loaded this entity before waiting for the account lock.
        entityManager.refresh(card);
        return card;
    }
}
