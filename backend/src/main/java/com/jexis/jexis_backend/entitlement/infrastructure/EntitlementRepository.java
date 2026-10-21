package com.jexis.jexis_backend.entitlement.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;

@Repository
public interface EntitlementRepository extends JpaRepository<Entitlement, UUID> {
}
