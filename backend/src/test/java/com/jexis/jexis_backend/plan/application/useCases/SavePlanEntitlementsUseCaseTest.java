package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.entitlement.application.useCases.GetEntitlementUseCase;
import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.plan.application.dto.SavePlanEntitlementDto;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.PlanEntitlement;
import com.jexis.jexis_backend.plan.domain.exceptions.PlanNotFoundException;
import com.jexis.jexis_backend.plan.infrastructure.PlanEntitlementRepository;
import com.jexis.jexis_backend.plan.infrastructure.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavePlanEntitlementsUseCaseTest {

    @Mock
    private PlanEntitlementRepository planEntitlementRepository;

    @Mock
    private PlanRepository planRepository;

    @Mock
    private GetEntitlementUseCase getEntitlementUseCase;

    @InjectMocks
    private SavePlanEntitlementsUseCase savePlanEntitlementsUseCase;

    private Plan plan;
    private UUID planId;
    private UUID entitlementId1;
    private UUID entitlementId2;
    private UUID entitlementId3;
    private Entitlement entitlement1;
    private Entitlement entitlement2;
    private Entitlement entitlement3;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();
        plan = new Plan("price_123", "Basic", "BASIC", "Description", true, com.jexis.jexis_backend.plan.domain.enums.PlanStatus.DRAFT);
        plan.setId(planId);

        entitlementId1 = UUID.randomUUID();
        entitlement1 = new Entitlement("storage", "NUMERIC", "Storage limit");
        entitlement1.setId(entitlementId1);

        entitlementId2 = UUID.randomUUID();
        entitlement2 = new Entitlement("features", "BOOLEAN", "Feature access");
        entitlement2.setId(entitlementId2);

        entitlementId3 = UUID.randomUUID();
        entitlement3 = new Entitlement("bandwidth", "NUMERIC", "Bandwidth limit");
        entitlement3.setId(entitlementId3);
    }

    @Test
    void execute_WhenPlanNotFound_ShouldThrowPlanNotFoundException() {
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        List<SavePlanEntitlementDto> dtos = List.of(new SavePlanEntitlementDto(entitlementId1, "10GB"));

        assertThatThrownBy(() -> savePlanEntitlementsUseCase.execute(planId, dtos))
                .isInstanceOf(PlanNotFoundException.class);
    }

    @Test
    void execute_WhenAddingNewEntitlements_ShouldSaveAndReturnList() {
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planEntitlementRepository.findByPlanId(planId)).thenReturn(Collections.emptyList());
        when(getEntitlementUseCase.execute(entitlementId1)).thenReturn(entitlement1);
        when(getEntitlementUseCase.execute(entitlementId2)).thenReturn(entitlement2);
        when(planEntitlementRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SavePlanEntitlementDto> dtos = List.of(
                new SavePlanEntitlementDto(entitlementId1, "10GB"),
                new SavePlanEntitlementDto(entitlementId2, "true")
        );

        List<PlanEntitlement> result = savePlanEntitlementsUseCase.execute(planId, dtos);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getValue()).isEqualTo("10GB");
        assertThat(result.get(1).getValue()).isEqualTo("true");
        verify(planEntitlementRepository, never()).deleteAll(any());
        verify(planEntitlementRepository).saveAll(any());
    }

    @Test
    void execute_WhenEntitlementAlreadyExists_ShouldUpdateExistingValue() {
        PlanEntitlement existing = new PlanEntitlement(plan, entitlement1, "5GB");
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planEntitlementRepository.findByPlanId(planId)).thenReturn(List.of(existing));
        when(planEntitlementRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SavePlanEntitlementDto> dtos = List.of(
                new SavePlanEntitlementDto(entitlementId1, "20GB")
        );

        List<PlanEntitlement> result = savePlanEntitlementsUseCase.execute(planId, dtos);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isEqualTo("20GB");
        assertThat(result.get(0)).isSameAs(existing);
        verify(getEntitlementUseCase, never()).execute(any());
        verify(planEntitlementRepository, never()).deleteAll(any());
        verify(planEntitlementRepository).saveAll(any());
    }

    @Test
    void execute_WhenExistingEntitlementsOmitted_ShouldDeleteOmittedEntitlements() {
        PlanEntitlement existing1 = new PlanEntitlement(plan, entitlement1, "5GB");
        PlanEntitlement existing2 = new PlanEntitlement(plan, entitlement2, "true");
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planEntitlementRepository.findByPlanId(planId)).thenReturn(List.of(existing1, existing2));
        when(planEntitlementRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SavePlanEntitlementDto> dtos = List.of(
                new SavePlanEntitlementDto(entitlementId1, "10GB")
        );

        List<PlanEntitlement> result = savePlanEntitlementsUseCase.execute(planId, dtos);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValue()).isEqualTo("10GB");
        verify(planEntitlementRepository).deleteAll(List.of(existing2));
        verify(planEntitlementRepository).saveAll(any());
    }

    @Test
    void execute_WhenEmptyDtoList_ShouldDeleteAllExistingAndReturnEmptyList() {
        PlanEntitlement existing1 = new PlanEntitlement(plan, entitlement1, "5GB");
        PlanEntitlement existing2 = new PlanEntitlement(plan, entitlement2, "true");
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planEntitlementRepository.findByPlanId(planId)).thenReturn(List.of(existing1, existing2));

        List<PlanEntitlement> result = savePlanEntitlementsUseCase.execute(planId, Collections.emptyList());

        assertThat(result).isEmpty();
        verify(planEntitlementRepository).deleteAll(List.of(existing1, existing2));
        verify(planEntitlementRepository, never()).saveAll(any());
    }

    @Test
    void execute_WhenMixedNewExistingAndDeletedEntitlements_ShouldHandleAll() {
        PlanEntitlement existing1 = new PlanEntitlement(plan, entitlement1, "5GB");
        PlanEntitlement existing3 = new PlanEntitlement(plan, entitlement3, "100GB");
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planEntitlementRepository.findByPlanId(planId)).thenReturn(List.of(existing1, existing3));
        when(getEntitlementUseCase.execute(entitlementId2)).thenReturn(entitlement2);
        when(planEntitlementRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SavePlanEntitlementDto> dtos = List.of(
                new SavePlanEntitlementDto(entitlementId1, "20GB"),
                new SavePlanEntitlementDto(entitlementId2, "true")
        );

        List<PlanEntitlement> result = savePlanEntitlementsUseCase.execute(planId, dtos);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getValue()).isEqualTo("20GB");
        assertThat(result.get(0)).isSameAs(existing1);
        assertThat(result.get(1).getValue()).isEqualTo("true");
        assertThat(result.get(1).getEntitlement()).isSameAs(entitlement2);
        verify(planEntitlementRepository).deleteAll(List.of(existing3));
        verify(getEntitlementUseCase, times(1)).execute(entitlementId2);
        verify(planEntitlementRepository).saveAll(any());
    }
}
