package com.jexis.jexis_backend.externalAccount.infrastructure;

import com.jexis.jexis_backend.externalAccount.domain.entities.ExternalAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ExternalAccountRepository extends JpaRepository<ExternalAccount, UUID> {
    Optional<ExternalAccount> findByStripeExternalAccountIdAndIsDeletedFalse(String stripeExternalAccountId);

    Optional<ExternalAccount> findByIdAndIsDeletedFalse(UUID id);

    @Query("""
            SELECT e FROM ExternalAccount e
            WHERE (
                  :search IS NULL
                  OR lower(cast(e.id as text)) LIKE concat('%', lower(cast(:search as text)), '%')
                  OR lower(e.bankName) LIKE concat('%', lower(cast(:search as text)), '%')
                  OR lower(e.last4) LIKE concat('%', lower(cast(:search as text)), '%')
                  OR lower(e.country) LIKE concat('%', lower(cast(:search as text)), '%')
              )
              AND (:isDefault IS NULL OR e.isDefault = :isDefault)
            """)
    Page<ExternalAccount> findExternalAccountsWithFilters(
            Pageable pageable,
            @Param("search") String search,
            @Param("isDefault") Boolean isDefault);

    @Query("""
            SELECT e FROM ExternalAccount e
            WHERE e.isDeleted = false
              AND (:accountId IS NULL OR e.account.id = :accountId)
              AND (
                  :search IS NULL
                  OR lower(cast(e.id as text)) LIKE concat('%', lower(cast(:search as text)), '%')
                  OR lower(e.bankName) LIKE concat('%', lower(cast(:search as text)), '%')
                  OR lower(e.last4) LIKE concat('%', lower(cast(:search as text)), '%')
                  OR lower(e.country) LIKE concat('%', lower(cast(:search as text)), '%')
              )
              AND (:isDefault IS NULL OR e.isDefault = :isDefault)
            """)
    Page<ExternalAccount> findAccountExternalAccountsWithFilters(
            Pageable pageable,
            @Param("accountId") UUID accountId,
            @Param("search") String search,
            @Param("isDefault") Boolean isDefault);
}
