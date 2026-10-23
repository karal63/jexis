package com.jexis.jexis_backend.member.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.card.application.useCases.DeleteCardUseCase;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.exceptions.MemberNotFoundException;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;

@Service
@RequiredArgsConstructor
public class RemoveMemberUseCase {
    private final MemberRepository repo;
    private final CardRepository cards;
    private final DeleteCardUseCase deleteCard;
    private final LockAccountUseCase lockAccount;

    @Transactional
    public void execute(UUID id) {
        Member member = repo.findById(id).orElseThrow(MemberNotFoundException::new);
        // Card issuance and replacement use this same account lock.
        lockAccount.execute(member.getAccount().getId());
        for (var card : cards.findByUserIdAndCardHolderAccountIdAndIsDeletedFalse(
                member.getUser().getId(), member.getAccount().getId())) {
            // Cancel at Stripe before removing membership; failures must abort removal.
            deleteCard.execute(card.getId());
        }
        repo.deleteById(id);
    }
}
