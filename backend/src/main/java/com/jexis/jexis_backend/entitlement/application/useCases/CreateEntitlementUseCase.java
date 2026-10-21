package com.jexis.jexis_backend.entitlement.application.useCases;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.entitlement.application.dto.CreateEntitlementDto;
import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.entitlement.infrastructure.EntitlementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateEntitlementUseCase {
    private final EntitlementRepository entitlementRepository;

    @Transactional
    public Entitlement execute(CreateEntitlementDto dto) {
        Entitlement entitlement = new Entitlement(
                dto.getKey(),
                dto.getType(),
                dto.getDescription());
        return entitlementRepository.save(entitlement);
    }
}
