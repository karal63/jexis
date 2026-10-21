package com.jexis.jexis_backend.subscription.presentation;

import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.subscription.application.dto.SetSubscriptionPaymentMethodDto;
import com.jexis.jexis_backend.subscription.application.dto.SubscriptionPageAdminResponseDto;
import com.jexis.jexis_backend.subscription.application.dto.SubscriptionPageResponseDto;
import com.jexis.jexis_backend.subscription.application.dto.SubscriptionResponseDto;
import com.jexis.jexis_backend.subscription.application.dto.EffectiveSubscriptionEntitlementDto;
import com.jexis.jexis_backend.subscription.application.dto.CreateCheckoutDto;
import com.jexis.jexis_backend.subscription.application.dto.CreateCheckoutResult;
import com.jexis.jexis_backend.subscription.application.dto.CreateSubscriptionDto;
import com.jexis.jexis_backend.subscription.application.dto.SaveSubscriptionEntitlementDto;
import com.jexis.jexis_backend.subscription.application.useCases.*;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.exceptions.SubscriptionNotFoundException;
import com.jexis.jexis_backend.subscription.domain.entities.SubscriptionEntitlement;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {
    private final GetAllSubscriptionsUseCase getAllSubscriptionsUseCase;
    private final CreateCheckoutUseCase createCheckoutUseCase;
    private final GetSubscriptionUseCase getSubscriptionUseCase;
    private final CreateSubscriptionUseCase createSubscriptionUseCase;
    private final SetSubscriptionPaymentMethodUseCase setSubscriptionPaymentMethodUseCase;
    private final ScheduleSubscriptionCancellationUseCase scheduleSubscriptionCancellationUseCase;
    private final CancelScheduledDowngradeUseCase cancelScheduledDowngradeUseCase;
    private final GetSubscriptionEntitlementsUseCase getSubscriptionEntitlementsUseCase;
    private final GetEffectiveSubscriptionEntitlementsUseCase getEffectiveSubscriptionEntitlementsUseCase;
    private final SaveSubscriptionEntitlementsUseCase saveSubscriptionEntitlementsUseCase;
    private final DtoHelper dtoHelper;
    private final GetMySubscriptionsUseCase getMySubscriptionsUseCase;
    private final GetActiveAccountSubscriptionUseCase getActiveAccountSubscriptionUseCase;

    @GetMapping("/admin/subscriptions")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public SubscriptionPageAdminResponseDto list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return mapToPageAdminResponse(getAllSubscriptionsUseCase.execute(page, pageSize));
    }

    @GetMapping("/admin/subscriptions/{id}/entitlements")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public List<SubscriptionEntitlement> listEntitlements(@PathVariable UUID id) {
        return getSubscriptionEntitlementsUseCase.execute(id);
    }

    @PostMapping("/admin/subscriptions/{id}/entitlements")
    @PreAuthorize("@userAuthorization.isAdmin(authentication.principal.roles())")
    public List<SubscriptionEntitlement> saveEntitlements(@PathVariable UUID id, @Valid @RequestBody List<SaveSubscriptionEntitlementDto> dtos) {
        return saveSubscriptionEntitlementsUseCase.execute(id, dtos);
    }

    @GetMapping("/subscriptions/{id}")
    @PreAuthorize("@subscriptionAuthorization.canView(authentication.principal.id(), #id)")
    public SubscriptionResponseDto get(@PathVariable UUID id) {
        return dtoHelper.toSubscriptionDtoWithOptionalPaymentMethod(getSubscriptionUseCase.execute(id));
    }

    @GetMapping("/subscriptions/{id}/entitlements")
    @PreAuthorize("@subscriptionAuthorization.canView(authentication.principal.id(), #id)")
    public List<EffectiveSubscriptionEntitlementDto> listEffectiveEntitlements(@PathVariable UUID id) {
        return getEffectiveSubscriptionEntitlementsUseCase.execute(id);
    }

    @PostMapping("/subscriptions/checkout")
    @PreAuthorize("@subscriptionAuthorization.canCheckout(authentication.principal.id(), #dto.accountId)")
    public ResponseEntity<String> createCheckoutSession(@Valid @RequestBody CreateCheckoutDto dto, @AuthenticationPrincipal AuthUser user) {
        CreateCheckoutResult result = createCheckoutUseCase.execute(dto, user.id());
        if (result.directSubscriptionChange()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result.checkoutUrl());
    }

    @PostMapping("/subscriptions/create")
    @PreAuthorize("@subscriptionAuthorization.canCreate(authentication.principal.id(), #dto.accountId)")
    public void create(@Valid @RequestBody CreateSubscriptionDto dto, @AuthenticationPrincipal AuthUser user) {
        createSubscriptionUseCase.execute(dto, user.id());
    }

    @PostMapping("/subscriptions/{id}/payment-method")
    @PreAuthorize("@subscriptionAuthorization.canUpdate(authentication.principal.id(), #id)")
    public void changePaymentMethod(@PathVariable UUID id, @Valid @RequestBody SetSubscriptionPaymentMethodDto dto, @AuthenticationPrincipal AuthUser user) {
        setSubscriptionPaymentMethodUseCase.execute(id, dto, user.id());
    }

    @PostMapping("/subscriptions/{id}/cancel")
    @PreAuthorize("@subscriptionAuthorization.canUpdate(authentication.principal.id(), #id)")
    public void cancel(@PathVariable UUID id) {
        scheduleSubscriptionCancellationUseCase.execute(id);
    }

    @PostMapping({"/subscriptions/{id}/cancel-downgrade", "/subscriptions/{id}/cancel-scheduled-downgrade"})
    @PreAuthorize("@subscriptionAuthorization.canUpdate(authentication.principal.id(), #id)")
    public void cancelDowngrade(@PathVariable UUID id) {
        cancelScheduledDowngradeUseCase.execute(id);
    }

    /**
     * Get all subscriptions of the authenticated user
     *
     * @param user user dto
     * @return subscription list
     */
    @GetMapping("/me/subscriptions")
    public SubscriptionPageResponseDto getMySubscriptions(
            @AuthenticationPrincipal AuthUser user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return mapToPageResponse(getMySubscriptionsUseCase.execute(user.id(), page, pageSize));
    }

    /**
     * Get active account subscription
     *
     * @param accountId account id
     * @return active subscription
     */
    @GetMapping("/accounts/{accountId}/active-subscription")
    @PreAuthorize("@subscriptionAuthorization.canViewActive(authentication.principal.id(), #accountId)")
    public SubscriptionResponseDto getAccountSubscription(@PathVariable UUID accountId) {
        Subscription subscription = getActiveAccountSubscriptionUseCase.execute(accountId);
        if (subscription == null) {
            throw new SubscriptionNotFoundException();
        }
        return dtoHelper.toSubscriptionDtoWithOptionalPaymentMethod(subscription);
    }

    private SubscriptionPageResponseDto mapToPageResponse(Page<Subscription> subscriptionsPage) {
        List<SubscriptionResponseDto> items = subscriptionsPage.getContent().stream()
                .map(dtoHelper::toSubscriptionDtoWithOptionalPaymentMethod)
                .toList();
        return new SubscriptionPageResponseDto(
                items,
                subscriptionsPage.getNumber(),
                subscriptionsPage.getSize(),
                subscriptionsPage.getTotalElements(),
                subscriptionsPage.getTotalPages()
        );
    }

    private SubscriptionPageAdminResponseDto mapToPageAdminResponse(Page<Subscription> subscriptionsPage) {
        List<SubscriptionResponseDto> items = subscriptionsPage.getContent().stream()
                .map(dtoHelper::toSubscriptionDtoWithOptionalPaymentMethod)
                .toList();
        return new SubscriptionPageAdminResponseDto(
                items,
                subscriptionsPage.getNumber(),
                subscriptionsPage.getSize(),
                subscriptionsPage.getTotalElements(),
                subscriptionsPage.getTotalPages()
        );
    }
}
