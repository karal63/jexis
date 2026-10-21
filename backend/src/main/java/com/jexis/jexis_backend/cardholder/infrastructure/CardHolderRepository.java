package com.jexis.jexis_backend.cardholder.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.cardholder.domain.enums.CardHolderStatus;

public interface CardHolderRepository extends JpaRepository<CardHolder, UUID> {
    long countByAccountIdAndIsDeletedFalse(UUID accountId);

    Optional<CardHolder> findByUserEmailAndAccountId(String email, UUID accountId);

    List<CardHolder> findAllByAccountIdAndIsDeletedFalse(UUID accountId);

    Optional<CardHolder> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            SELECT c FROM CardHolder c
            WHERE coalesce(c.isDeleted, false) = false
              AND (
                  :search IS NULL
                  OR lower(cast(c.id as text)) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(c.name) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingAddressLine1, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingAddressLine2, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingCity, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingState, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingCountry, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingPostalCode, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(c.user.firstName) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(c.user.lastName) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(c.user.email) LIKE concat('%', CAST(:search AS text), '%')
              )
              AND (:status IS NULL OR c.status = :status)
            """)
    Page<CardHolder> findCardHoldersWithFilters(
            Pageable pageable,
            @Param("search") String search,
            @Param("status") CardHolderStatus status);

    @Query("""
            SELECT c FROM CardHolder c
            WHERE c.isDeleted = false
              AND c.account.id = :accountId
              AND (
                  :search IS NULL
                  OR lower(cast(c.id as text)) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(c.name) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingAddressLine1, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingAddressLine2, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingCity, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingState, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingCountry, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(coalesce(c.billingPostalCode, '')) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(c.user.firstName) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(c.user.lastName) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(c.user.email) LIKE concat('%', CAST(:search AS text), '%')
              )
              AND (:status IS NULL OR c.status = :status)
            """)
    Page<CardHolder> findAccountCardHoldersWithFilters(
            @Param("accountId") UUID accountId,
            Pageable pageable,
            @Param("search") String search,
            @Param("status") CardHolderStatus status);
}
