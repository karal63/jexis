package com.jexis.jexis_backend.subscription.application.useCases;

import com.jexis.jexis_backend.entitlement.application.useCases.GetEntitlementUseCase;
import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.subscription.application.dto.SaveSubscriptionEntitlementDto;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.entities.SubscriptionEntitlement;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionEntitlementRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SaveSubscriptionEntitlementsUseCase {
    private final SubscriptionEntitlementRepository subscriptionEntitlementRepository;
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final GetEntitlementUseCase getEntitlementUseCase;

    @Transactional
    public List<SubscriptionEntitlement> execute(UUID subscriptionId, List<SaveSubscriptionEntitlementDto> dtos) {
        Subscription subscription = getSubscriptionUseCase.execute(subscriptionId);

        List<SubscriptionEntitlement> existingList = subscriptionEntitlementRepository.findBySubscriptionId(subscriptionId);
        Map<UUID, SubscriptionEntitlement> existingMap = existingList.stream()
                .collect(Collectors.toMap(
                        se -> se.getEntitlement().getId(),
                        se -> se,
                        (a, b) -> a
                ));

        Set<UUID> dtoEntitlementIds = dtos.stream()
                .map(SaveSubscriptionEntitlementDto::entitlementId)
                .collect(Collectors.toSet());

        List<SubscriptionEntitlement> toDelete = existingList.stream()
                .filter(se -> !dtoEntitlementIds.contains(se.getEntitlement().getId()))
                .toList();

        if (!toDelete.isEmpty()) {
            subscriptionEntitlementRepository.deleteAll(toDelete);
        }

        List<SubscriptionEntitlement> toSave = new ArrayList<>();
        for (SaveSubscriptionEntitlementDto dto : dtos) {
            SubscriptionEntitlement subscriptionEntitlement = existingMap.get(dto.entitlementId());
            if (subscriptionEntitlement != null) {
                subscriptionEntitlement.setValue(dto.value());
                toSave.add(subscriptionEntitlement);
            } else {
                Entitlement entitlement = getEntitlementUseCase.execute(dto.entitlementId());
                SubscriptionEntitlement newSubscriptionEntitlement = new SubscriptionEntitlement(subscription, entitlement, dto.value());
                toSave.add(newSubscriptionEntitlement);
            }
        }

        if (toSave.isEmpty()) {
            return List.of();
        }

        return subscriptionEntitlementRepository.saveAll(toSave);
    }
}
