package com.jexis.jexis_backend.member.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.enums.Role;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    long countByAccountId(UUID accountId);
    public List<Member> findAllByAccountId(UUID accountId);

    public Optional<Member> findByAccountIdAndUserId(UUID accountId, UUID userId);

    public Boolean existsByUserIdAndAccountId(UUID userId, UUID accountId);

    public boolean existsByUserIdAndAccountIdAndRole(UUID userId, UUID accountId, Role role);

    @Query("""
            SELECT m FROM Member m
            WHERE
               (
                  :search IS NULL
                  OR cast(m.id as string) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(m.user.firstName) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(m.user.lastName) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(m.user.email) LIKE concat('%', CAST(:search AS text), '%')
              )
              AND (:role IS NULL OR m.role = :role)
        """)
    Page<Member> findMembersWithFilters(
            Pageable pageable,
            @Param("search") String search,
            @Param("role") Role role);

    @Query("""
            SELECT m FROM Member m
            WHERE m.account.id = :accountId
              AND (
                  :search IS NULL
                  OR cast(m.id as string) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(m.user.firstName) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(m.user.lastName) LIKE concat('%', CAST(:search AS text), '%')
                  OR lower(m.user.email) LIKE concat('%', CAST(:search AS text), '%')
              )
              AND (:role IS NULL OR m.role = :role)
        """)
    Page<Member> findAccountMembersWithFilters(
            Pageable pageable,
            @Param("accountId") UUID accountId,
            @Param("search") String search,
            @Param("role") Role role);
}
