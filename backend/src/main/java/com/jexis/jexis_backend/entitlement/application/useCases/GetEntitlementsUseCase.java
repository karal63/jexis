package com.jexis.jexis_backend.entitlement.application.useCases;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.entitlement.infrastructure.EntitlementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetEntitlementsUseCase {
    private final EntitlementRepository entitlementRepository;

    @Transactional(readOnly = true)
    public List<Entitlement> execute() {
        return entitlementRepository.findAll();
    }
}
