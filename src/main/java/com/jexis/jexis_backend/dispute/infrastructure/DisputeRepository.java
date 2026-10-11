package com.jexis.jexis_backend.dispute.infrastructure;

import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.dispute.application.dto.DisputeReason;
import com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {
    Optional<Dispute> findByStripeDisputeId(String stripeDisputeId);

    @Query("""
            SELECT d FROM Dispute d
            WHERE (
                  :search IS NULL
                  OR lower(cast(d.id as text)) LIKE concat('%', lower(cast(:search as text)), '%')
              )
              AND (:reason IS NULL OR d.reason = :reason)
              AND (:status IS NULL OR d.status = :status)
            """)
    Page<Dispute> findDisputesWithFilters(
            Pageable pageable,
            @Param("search") String search,
            @Param("reason") DisputeReason reason,
            @Param("status") DisputeStatus status);

    @Query("""
                    SELECT d FROM Dispute d
                    WHERE (:cardId IS NULL OR d.transaction.card.id = :cardId)
                      AND (
                          :search IS NULL
                          OR lower(cast(d.id as text)) LIKE concat('%', lower(cast(:search as text)), '%')
                      )
                      AND (:reason IS NULL OR d.reason = :reason)
                      AND (:status IS NULL OR d.status = :status)
                    """)
    Page<Dispute> findCardDisputesWithFilters(
            Pageable pageable,
            @Param("cardId") UUID cardId,
            @Param("search") String search,
            @Param("reason") DisputeReason reason,
            @Param("status") DisputeStatus status);

    @Query("""
                    SELECT d FROM Dispute d
                    WHERE d.transaction.card.user.id = :userId
                  AND d.wallet.account.id = :accountId
                  AND d.transaction.wallet.account.id = :accountId
                  AND d.transaction.card.treasuryAccount.account.id = :accountId
                      AND (
                          :search IS NULL
                          OR lower(cast(d.id as text)) LIKE concat('%', lower(cast(:search as text)), '%')
                      )
                      AND (:reason IS NULL OR d.reason = :reason)
                      AND (:status IS NULL OR d.status = :status)
                    """)
    Page<Dispute> findMemberDisputesWithFilters(
            Pageable pageable,
            @Param("accountId") UUID accountId,
            @Param("userId") UUID userId,
            @Param("search") String search,
            @Param("reason") DisputeReason reason,
            @Param("status") DisputeStatus status);
}
