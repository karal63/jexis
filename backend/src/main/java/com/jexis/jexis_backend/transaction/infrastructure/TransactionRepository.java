package com.jexis.jexis_backend.transaction.infrastructure;

import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionDirection;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionStatus;
import com.jexis.jexis_backend.transaction.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByWalletId(UUID walletId);
    List<Transaction> findByCardId(UUID cardId);
    Optional<Transaction> findByStripeObjectId(String stripeObjectId);

    @Query("""
            SELECT t FROM Transaction t
            WHERE (
                :search IS NULL
                OR lower(CAST(t.id AS string)) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.bankName) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.bankAccountLast4) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.routingNumber) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantName) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCategory) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCity) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCountry) LIKE concat('%', lower(CAST(:search AS text)), '%')
            )
            AND (:type IS NULL OR t.type = :type)
            AND (:status IS NULL OR t.status = :status)
            AND (:direction IS NULL OR t.direction = :direction)
            """)
    Page<Transaction> findTransactionsWithFilters(
            Pageable pageable,
            @Param("search") String search,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("direction") TransactionDirection direction);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.wallet.id = :walletId
              AND (
                :search IS NULL
                OR lower(CAST(t.id AS string)) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.bankName) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.bankAccountLast4) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.routingNumber) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantName) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCategory) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCity) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCountry) LIKE concat('%', lower(CAST(:search AS text)), '%')
              )
            AND (:type IS NULL OR t.type = :type)
            AND (:status IS NULL OR t.status = :status)
            AND (:direction IS NULL OR t.direction = :direction)
            """)
    Page<Transaction> findWalletTransactionsWithFilters(
            Pageable pageable,
            @Param("walletId") UUID walletId,
            @Param("search") String search,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("direction") TransactionDirection direction);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.card.id = :cardId
              AND (
                :search IS NULL
                OR lower(CAST(t.id AS string)) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.bankName) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.bankAccountLast4) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.routingNumber) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantName) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCategory) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCity) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCountry) LIKE concat('%', lower(CAST(:search AS text)), '%')
              )
            AND (:type IS NULL OR t.type = :type)
            AND (:status IS NULL OR t.status = :status)
            AND (:direction IS NULL OR t.direction = :direction)
            """)
    Page<Transaction> findCardTransactionsWithFilters(
            Pageable pageable,
            @Param("cardId") UUID cardId,
            @Param("search") String search,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("direction") TransactionDirection direction);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.card.user.id = :userId
                  AND t.wallet.account.id = :accountId
                  AND t.card.treasuryAccount.account.id = :accountId
              AND (
                :search IS NULL
                OR lower(CAST(t.id AS string)) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.bankName) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.bankAccountLast4) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.routingNumber) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantName) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCategory) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCity) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(t.merchantCountry) LIKE concat('%', lower(CAST(:search AS text)), '%')
              )
            AND (:type IS NULL OR t.type = :type)
            AND (:status IS NULL OR t.status = :status)
            AND (:direction IS NULL OR t.direction = :direction)
            """)
    Page<Transaction> findMemberTransactionsWithFilters(
            Pageable pageable,
            @Param("accountId") UUID accountId,
            @Param("userId") UUID userId,
            @Param("search") String search,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            @Param("direction") TransactionDirection direction);
}
