package com.jexis.jexis_backend.authorization.infrastructure;

import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.authorization.domain.enums.AuthorizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthorizationRepository extends JpaRepository<Authorization, UUID> {
    List<Authorization> findByWalletId(UUID walletId);
    Optional<Authorization> findByStripeAuthorizationId(String stripeAuthorizationId);

    @Query("""
            SELECT a FROM Authorization a 
                WHERE (
                    :search IS NULL 
                    OR lower(a.merchantCategory) LIKE concat('%', CAST(:search AS text), '%') 
                    OR lower(a.merchantName) LIKE concat('%', CAST(:search AS text), '%')
                    OR lower(a.merchantCity) LIKE concat('%', CAST(:search AS text), '%')
                    OR lower(a.merchantCountry) LIKE concat('%', CAST(:search AS text), '%')
                )
                AND (:approved IS NULL OR a.approved = :approved)
                AND (:status IS NULL OR a.status = :status)
            """)
    Page<Authorization> findAuthorizationsWithFilters(
            Pageable pageable,
            @Param("search") String search,
            @Param("approved") Boolean approved,
            @Param("status") AuthorizationStatus status
    );

    @Query("""
            SELECT a FROM Authorization a 
                WHERE a.wallet.id = :walletId AND (
                    :search IS NULL 
                    OR lower(a.merchantCategory) LIKE concat('%', CAST(:search AS text), '%') 
                    OR lower(a.merchantName) LIKE concat('%', CAST(:search AS text), '%')
                    OR lower(a.merchantCity) LIKE concat('%', CAST(:search AS text), '%')
                    OR lower(a.merchantCountry) LIKE concat('%', CAST(:search AS text), '%')
                )
                AND (:approved IS NULL OR a.approved = :approved)
                AND (:status IS NULL OR a.status = :status)
            """)
    Page<Authorization> findWalletAuthorizationsWithFilters(
            @Param("walletId") UUID walletId,
            Pageable pageable,
            @Param("search") String search,
            @Param("approved") Boolean approved,
            @Param("status") AuthorizationStatus status);

    @Query("""
            SELECT a FROM Authorization a
                WHERE a.card.user.id = :userId
                  AND a.wallet.account.id = :accountId
                  AND a.card.treasuryAccount.account.id = :accountId
                AND (
                    :search IS NULL
                    OR lower(a.merchantCategory) LIKE concat('%', CAST(:search AS text), '%')
                    OR lower(a.merchantName) LIKE concat('%', CAST(:search AS text), '%')
                    OR lower(a.merchantCity) LIKE concat('%', CAST(:search AS text), '%')
                    OR lower(a.merchantCountry) LIKE concat('%', CAST(:search AS text), '%')
                )
                AND (:approved IS NULL OR a.approved = :approved)
                AND (:status IS NULL OR a.status = :status)
            """)
    Page<Authorization> findMemberAuthorizationsWithFilters(
            @Param("accountId") UUID accountId,
            @Param("userId") UUID userId,
            Pageable pageable,
            @Param("search") String search,
            @Param("approved") Boolean approved,
            @Param("status") AuthorizationStatus status);
}
