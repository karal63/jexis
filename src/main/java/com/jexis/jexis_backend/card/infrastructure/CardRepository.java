package com.jexis.jexis_backend.card.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;

public interface CardRepository extends JpaRepository<Card, UUID> {
    @Query("select c.cardHolder.account.id from Card c where c.id = :id")
    Optional<UUID> findAccountId(@Param("id") UUID id);
    // Pending replacements retain their slot until the replacement is persisted.
    @Query("""
            select count(c) from Card c where c.cardHolder.account.id = :accountId
              and (c.replacementPending = true or
                   (c.isDeleted = false and c.status in :statuses))
            """)
    long countResourceUsage(@Param("accountId") UUID accountId,
                            @Param("statuses") List<CardStatus> statuses);
    List<Card> findByCardHolderAccountIdAndIsDeletedFalse(UUID accountId);

    Optional<Card> findByIdAndIsDeletedFalse(UUID id);

    Optional<Card> findByStripeCardIdAndIsDeletedFalse(String stripeCardId);

    List<Card> findAllByTreasuryAccountIdAndIsDeletedFalse(UUID treasuryAccountId);

    @Query("""
                SELECT c FROM Card c
                WHERE 
                   (
                      :search IS NULL 
                      OR lower(c.last4) LIKE concat('%', CAST(:search AS text), '%')
                      OR lower(c.cardHolder.name) LIKE concat('%', CAST(:search AS text), '%')
                  )
                  AND (:status IS NULL OR c.status = :status)
                  AND (:brand IS NULL OR lower(c.brand) = lower(CAST(:brand AS text)))
                  AND (:type IS NULL OR lower(c.type) = lower(CAST(:type AS text)))
            """)
    Page<Card> findCardsWithFilters(
            Pageable pageable,
            @Param("search") String search,
            @Param("status") CardStatus status,
            @Param("brand") String brand,
            @Param("type") String type);

    @Query("""
                SELECT c FROM Card c
                WHERE c.isDeleted = false
                  AND c.treasuryAccount.id = :walletId
                  AND (
                      :search IS NULL 
                      OR lower(c.last4) LIKE concat('%', CAST(:search AS text), '%')
                      OR lower(c.cardHolder.name) LIKE concat('%', CAST(:search AS text), '%')
                  )
                  AND (:status IS NULL OR c.status = :status)
                  AND (:brand IS NULL OR lower(c.brand) = lower(CAST(:brand AS text)))
                  AND (:type IS NULL OR lower(c.type) = lower(CAST(:type AS text)))
            """)
    Page<Card> findWalletCardsWithFilters(
            Pageable pageable,
            @Param("walletId") UUID walletId,
            @Param("search") String search,
            @Param("status") CardStatus status,
            @Param("brand") String brand,
            @Param("type") String type);

    @Query("""
                SELECT c FROM Card c
                WHERE c.isDeleted = false
                  AND c.user.id = :userId
                  AND c.treasuryAccount.account.id = :accountId AND c.cardHolder.account.id = :accountId
                  AND (
                      :search IS NULL
                      OR lower(c.last4) LIKE concat('%', CAST(:search AS text), '%')
                      OR lower(c.cardHolder.name) LIKE concat('%', CAST(:search AS text), '%')
                  )
                  AND (:status IS NULL OR c.status = :status)
                  AND (:brand IS NULL OR lower(c.brand) = lower(CAST(:brand AS text)))
                  AND (:type IS NULL OR lower(c.type) = lower(CAST(:type AS text)))
            """)
    Page<Card> findMemberCardsWithFilters(
            Pageable pageable,
            @Param("accountId") UUID accountId,
            @Param("userId") UUID userId,
            @Param("search") String search,
            @Param("status") CardStatus status,
            @Param("brand") String brand,
            @Param("type") String type);
}
