package com.jexis.jexis_backend.entitlement.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.entitlement.application.dto.UpdateEntitlementDto;
import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.entitlement.domain.exceptions.EntitlementNotFoundException;
import com.jexis.jexis_backend.entitlement.infrastructure.EntitlementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateEntitlementUseCase {
    private final EntitlementRepository entitlementRepository;

    @Transactional
    public Entitlement execute(UUID id, UpdateEntitlementDto dto) {
        Entitlement entitlement = entitlementRepository.findById(id)
                .orElseThrow(EntitlementNotFoundException::new);

        entitlement.setKey(dto.getKey());
        entitlement.setType(dto.getType());
        entitlement.setDescription(dto.getDescription());

        return entitlementRepository.save(entitlement);
    }
}
