package com.jexis.jexis_backend.entitlement.application.useCases;

import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.entitlement.domain.exceptions.EntitlementNotFoundException;
import com.jexis.jexis_backend.entitlement.infrastructure.EntitlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetEntitlementUseCase {
    private final EntitlementRepository entitlementRepository;

    @Transactional(readOnly = true)
    public Entitlement execute(UUID id) {
        return entitlementRepository.findById(id).orElseThrow(EntitlementNotFoundException::new);
    }
}
