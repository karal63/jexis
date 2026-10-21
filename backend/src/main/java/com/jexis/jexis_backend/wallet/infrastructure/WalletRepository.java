package com.jexis.jexis_backend.wallet.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jexis.jexis_backend.wallet.domain.entities.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    long countByAccountIdAndIsDeletedFalse(UUID accountId);

    List<Wallet> findAllByAccountIdAndIsDeletedFalse(UUID accountId);

    Optional<Wallet> findByIdAndIsDeletedFalse(UUID accountId);

    Optional<Wallet> findByStripeFinancialAccountIdAndIsDeletedFalse(String stripeFinancialAccountId);

    @Query("""
            SELECT w FROM Wallet w
            WHERE (
                :search IS NULL
                OR lower(CAST(w.id AS string)) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(w.name) LIKE concat('%', lower(CAST(:search AS text)), '%')
            )
            """)
    Page<Wallet> findWalletsWithFilters(Pageable pageable, @Param("search") String search);

    @Query("""
            SELECT w FROM Wallet w
            WHERE w.account.id = :accountId
              AND w.isDeleted = false
              AND (
                :search IS NULL
                OR lower(CAST(w.id AS string)) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(w.name) LIKE concat('%', lower(CAST(:search AS text)), '%')
              )
            """)
    Page<Wallet> findAccountWalletsWithFilters(Pageable pageable, @Param("accountId") UUID accountId, @Param("search") String search);
}
