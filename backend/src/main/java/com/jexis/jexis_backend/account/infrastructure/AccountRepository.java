package com.jexis.jexis_backend.account.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jexis.jexis_backend.account.domain.entities.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id and a.isDeleted = false")
    Optional<Account> findForResourceUpdate(@Param("id") UUID id);

    List<Account> findAllByOwnerIdAndIsDeletedFalse(UUID ownerId);

    Optional<Account> findByIdAndIsDeletedFalse(UUID id);

    Optional<Account> findByConnectAccountIdAndIsDeletedFalse(String connectAccountId);

    // Paging support
    Page<Account> findAllByIsDeletedFalse(Pageable pageable);

    Page<Account> findAllByOwnerIdAndIsDeletedFalse(UUID ownerId, Pageable pageable);

    // Search across firstName, lastName and email (case-insensitive)
    @Query("select a from Account a where (lower(a.firstName) like lower(concat('%', :search, '%')) or lower(a.lastName) like lower(concat('%', :search, '%')) or lower(a.email) like lower(concat('%', :search, '%'))) ")
    Page<Account> searchAll(@Param("search") String search, Pageable pageable);

    @Query("select a from Account a where (coalesce(a.isDeleted, false) = false) and a.owner.id = :ownerId and (lower(a.firstName) like lower(concat('%', :search, '%')) or lower(a.lastName) like lower(concat('%', :search, '%')) or lower(a.email) like lower(concat('%', :search, '%'))) ")
    Page<Account> searchByOwner(@Param("ownerId") UUID ownerId, @Param("search") String search, Pageable pageable);

    @Query("""
                select m.account from Member m
                    where m.user.id = :userId
                        and (
                            :search is null
                            or lower(m.account.firstName) like lower(concat('%', CAST(:search AS text), '%')) 
                            or lower(m.account.lastName) like lower(concat('%', CAST(:search AS text), '%'))
                            or lower(m.account.email) like lower(concat('%', CAST(:search AS text), '%'))
                        ) and m.account.isDeleted = false
            """)
    Page<Account> searchAccountsByMember(
            @Param("userId") UUID userId,
            @Param("search") String search,
            Pageable pageable);
}
