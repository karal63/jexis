package com.jexis.jexis_backend.entitlement.presentation;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.jexis.jexis_backend.entitlement.application.dto.CreateEntitlementDto;
import com.jexis.jexis_backend.entitlement.application.dto.UpdateEntitlementDto;
import com.jexis.jexis_backend.entitlement.application.useCases.CreateEntitlementUseCase;
import com.jexis.jexis_backend.entitlement.application.useCases.DeleteEntitlementUseCase;
import com.jexis.jexis_backend.entitlement.application.useCases.GetEntitlementsUseCase;
import com.jexis.jexis_backend.entitlement.application.useCases.UpdateEntitlementUseCase;
import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/entitlements")
@RequiredArgsConstructor
@PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
public class EntitlementController {
    private final CreateEntitlementUseCase createEntitlementUseCase;
    private final GetEntitlementsUseCase getEntitlementsUseCase;
    private final UpdateEntitlementUseCase updateEntitlementUseCase;
    private final DeleteEntitlementUseCase deleteEntitlementUseCase;

    @PostMapping
    public Entitlement create(@Valid @RequestBody CreateEntitlementDto dto) {
        return createEntitlementUseCase.execute(dto);
    }

    @GetMapping
    public List<Entitlement> list() {
        return getEntitlementsUseCase.execute();
    }

    @PutMapping("/{id}")
    public Entitlement update(@PathVariable UUID id, @Valid @RequestBody UpdateEntitlementDto dto) {
        return updateEntitlementUseCase.execute(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteEntitlementUseCase.execute(id);
    }
}
