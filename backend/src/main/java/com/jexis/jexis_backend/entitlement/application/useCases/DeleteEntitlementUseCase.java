package com.jexis.jexis_backend.entitlement.application.useCases;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.entitlement.domain.exceptions.EntitlementNotFoundException;
import com.jexis.jexis_backend.entitlement.infrastructure.EntitlementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteEntitlementUseCase {
    private final EntitlementRepository entitlementRepository;

    @Transactional
    public void execute(UUID id) {
        Entitlement entitlement = entitlementRepository.findById(id)
                .orElseThrow(EntitlementNotFoundException::new);
        entitlementRepository.delete(entitlement);
    }
}
