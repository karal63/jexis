package com.jexis.jexis_backend.plan.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.jexis.jexis_backend.plan.domain.entities.Price;

@Repository
public interface PriceRepository extends JpaRepository<Price, UUID> {
    List<Price> findAllByPlanId(UUID planId);
    Optional<Price> findByStripePriceId(String stripePriceId);
}
