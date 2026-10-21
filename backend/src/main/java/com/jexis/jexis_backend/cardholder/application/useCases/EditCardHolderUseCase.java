package com.jexis.jexis_backend.cardholder.application.useCases;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.jexis.jexis_backend.cardholder.application.dto.EditCardHolderDto;
import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.cardholder.domain.exceptions.CardHolderNotFoundException;
import com.jexis.jexis_backend.cardholder.infrastructure.CardHolderRepository;
import com.jexis.jexis_backend.stripe.application.useCases.UpdateStripeCardHolderUseCase;

@Service
public class EditCardHolderUseCase {
    private final CardHolderRepository repo;
    private final UpdateStripeCardHolderUseCase updateStripeCardHolderUseCase;
    private final GetCardHolderUseCase getCardHolderUseCase;

    public EditCardHolderUseCase(CardHolderRepository repo,
            UpdateStripeCardHolderUseCase updateStripeCardHolderUseCase, GetCardHolderUseCase getCardHolderUseCase) {
        this.repo = repo;
        this.updateStripeCardHolderUseCase = updateStripeCardHolderUseCase;
        this.getCardHolderUseCase = getCardHolderUseCase;
    }

    public CardHolder execute(UUID id, EditCardHolderDto dto) {
        CardHolder cardHolder = getCardHolderUseCase.execute(id);

        // update stripe data
        updateStripeCardHolderUseCase.execute(cardHolder.getAccount().getConnectAccountId(),
                cardHolder.getStripeCardHolderId(), dto);

        // Billing address
        if (dto.billingAddress() != null) {
            if (dto.billingAddress().line1() != null) {
                cardHolder.setBillingAddressLine1(dto.billingAddress().line1());
            }
            if (dto.billingAddress().line2() != null) {
                cardHolder.setBillingAddressLine2(dto.billingAddress().line2());
            }
            if (dto.billingAddress().city() != null) {
                cardHolder.setBillingCity(dto.billingAddress().city());
            }
            if (dto.billingAddress().state() != null) {
                cardHolder.setBillingState(dto.billingAddress().state());
            }
            if (dto.billingAddress().country() != null) {
                cardHolder.setBillingCountry(dto.billingAddress().country());
            }
            if (dto.billingAddress().postalCode() != null) {
                cardHolder.setBillingPostalCode(dto.billingAddress().postalCode());
            }
        }

        // Spending controls
        if (dto.spendingControls() != null) {
            if (dto.spendingControls().spendingLimits() != null) {
                cardHolder.setSpendingLimits(dto.spendingControls().spendingLimits());
            }
        }

        if (dto.status() != null) {
            cardHolder.setStatus(dto.status());
        }

        return repo.save(cardHolder);
    }
}
