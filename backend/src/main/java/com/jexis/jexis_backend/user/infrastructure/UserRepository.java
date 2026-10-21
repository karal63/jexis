package com.jexis.jexis_backend.user.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.domain.enums.UserRole;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByEmailOrPhoneNumber(String email, String phoneNumber);

    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    Optional<User> findByEmail(String email);

    Optional<User> findByActivationTokenHash(String token);

    @Query("""
            SELECT u FROM User u
            WHERE (
                :search IS NULL
                OR lower(CAST(u.id AS string)) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(u.firstName) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(u.lastName) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(u.email) LIKE concat('%', lower(CAST(:search AS text)), '%')
                OR lower(u.phoneNumber) LIKE concat('%', lower(CAST(:search AS text)), '%')
            )
            AND (:role IS NULL OR :role MEMBER OF u.roles)
            AND (:isActivated IS NULL OR u.isActivated = :isActivated)
            """)
    Page<User> findUsersWithFilters(
            Pageable pageable,
            @Param("search") String search,
            @Param("role") UserRole role,
            @Param("isActivated") Boolean isActivated);
}