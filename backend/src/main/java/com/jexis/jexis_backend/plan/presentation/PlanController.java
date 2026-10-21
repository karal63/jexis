package com.jexis.jexis_backend.plan.presentation;

import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.plan.application.dto.*;
import com.jexis.jexis_backend.plan.application.useCases.*;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.PlanEntitlement;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/plans")
@RequiredArgsConstructor
@PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
public class PlanController {
    private final CreatePlanUseCase createPlanUseCase;
    private final GetAllPlansUseCase getAllPlansUseCase;
    private final GetPlanEntitlementsUseCase getPlanEntitlementsUseCase;
    private final SavePlanEntitlementsUseCase savePlanEntitlementsUseCase;
    private final DeletePlanUseCase deletePlanUseCase;
    private final UpdatePlanUseCase updatePlanUseCase;
    private final GetPlanPricesUseCase getPlanPricesUseCase;
    private final CreatePriceUseCase createPriceUseCase;
    private final GetPlanPriceUseCase getPlanPriceUseCase;
    private final UpdatePriceUseCase updatePriceUseCase;
    private final DtoHelper dtoHelper;
    private final SetDefaultPriceUseCase setDefaultPriceUseCase;
    private final PublishPlanUseCase publishPlanUseCase;

    @PostMapping
    public Plan create(@Valid @RequestBody CreatePlanDto dto) {
        return createPlanUseCase.execute(dto);
    }

    @GetMapping
    public List<PlanResponseDto> list() {
        List<Plan> plans = getAllPlansUseCase.execute();
        return plans.stream().map(dtoHelper::toPlanDto).toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        deletePlanUseCase.execute(id);
    }

    @PatchMapping("/{id}")
    public Plan update(@PathVariable UUID id, @Valid @RequestBody UpdatePlanDto dto) {
        return updatePlanUseCase.execute(id, dto);
    }

    @PatchMapping("/{id}/publish")
    public Plan publish(@PathVariable UUID id) {
        return publishPlanUseCase.execute(id);
    }

    @PatchMapping("/{planId}/set-default-price")
    public void setDefaultPrice(@PathVariable UUID planId, @Valid @RequestBody SetDefaultPriceDto dto) {
        setDefaultPriceUseCase.execute(planId, dto);
    }

    @GetMapping("/{id}/entitlements")
    public List<PlanEntitlement> listEntitlements(@PathVariable UUID id) {
        return getPlanEntitlementsUseCase.execute(id);
    }

    @PostMapping("/{id}/entitlements")
    public List<PlanEntitlement> savePlanEntitlements(@PathVariable UUID id, @Valid @RequestBody List<SavePlanEntitlementDto> dtos) {
        return savePlanEntitlementsUseCase.execute(id, dtos);
    }

    @GetMapping("/{id}/prices")
    public List<Price> getPlanPrices(@PathVariable UUID id) {
        return getPlanPricesUseCase.execute(id);
    }

    @PostMapping("/{id}/prices")
    public Price createPrice(@PathVariable UUID id, @Valid @RequestBody CreatePriceDto dto) {
        return createPriceUseCase.execute(id, dto);
    }

    @GetMapping("/prices/{priceId}")
    public Price getPrice(@PathVariable UUID priceId) {
        return getPlanPriceUseCase.execute(priceId);
    }

    @PatchMapping("/prices/{priceId}")
    public Price updatePrice(@PathVariable UUID priceId, @Valid @RequestBody UpdatePriceDto dto) {
        return updatePriceUseCase.execute(priceId, dto);
    }


}
