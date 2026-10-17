package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.application.dto.*;
import com.jexis.jexis_backend.plan.domain.entities.*;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import com.jexis.jexis_backend.plan.domain.exceptions.ProtectedFreePlanException;
import com.jexis.jexis_backend.plan.infrastructure.*;
import com.jexis.jexis_backend.stripe.application.useCases.plan.price.*;
import com.jexis.jexis_backend.stripe.application.useCases.plan.product.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FreePlanProtectionTest {
    private final PlanRepository plans = mock(PlanRepository.class);
    private final PriceRepository prices = mock(PriceRepository.class);
    private final GetPlanUseCase getPlan = mock(GetPlanUseCase.class);
    private final GetPlanPriceUseCase getPrice = mock(GetPlanPriceUseCase.class);
    private final UUID id = UUID.randomUUID();

    private Plan free(String stripeId) {
        Plan plan = new Plan(stripeId, "Free", Plan.FREE_CODE, "Default", true, PlanStatus.PUBLISHED);
        plan.setId(id);
        plan.setDefaultPrice(new Price(plan, null, "usd", 0L, "month", 1, true));
        return plan;
    }

    @ParameterizedTest @NullSource @ValueSource(strings = "prod_legacy")
    void preventsDeletionRegardlessOfStripeId(String stripeId) {
        var stripe = mock(DeleteStripeProductUseCase.class);
        when(getPlan.execute(id)).thenReturn(free(stripeId));
        assertThatThrownBy(() -> new DeletePlanUseCase(stripe, plans, getPlan).execute(id))
                .isInstanceOf(ProtectedFreePlanException.class);
        verifyNoInteractions(plans, stripe);
    }

    @ParameterizedTest @NullSource @ValueSource(strings = "prod_legacy")
    void preventsReplacingDefaultPriceAndCreatingNewPrices(String stripeId) {
        var createStripe = mock(CreateStripePriceUseCase.class);
        var defaultStripe = mock(SetStripeDefaultPriceUseCase.class);
        Plan plan = free(stripeId);
        Price original = plan.getDefaultPrice();
        when(getPlan.execute(id)).thenReturn(plan);
        assertThatThrownBy(() -> new SetDefaultPriceUseCase(getPlan, plans, getPrice, defaultStripe)
                .execute(id, new SetDefaultPriceDto(UUID.randomUUID())))
                .isInstanceOf(ProtectedFreePlanException.class);
        assertThatThrownBy(() -> new CreatePriceUseCase(prices, plans, getPlan, createStripe, defaultStripe)
                .execute(id, new CreatePriceDto()))
                .isInstanceOf(ProtectedFreePlanException.class);
        assertThat(plan.getDefaultPrice()).isSameAs(original);
        verifyNoInteractions(plans, prices, getPrice, createStripe, defaultStripe);
    }

    @Test void preventsDeactivatingFreePriceBeforeMutationOrStripeCalls() {
        var stripe = mock(UpdateStripePriceUseCase.class);
        Price price = free("prod_legacy").getDefaultPrice();
        price.setStripePriceId("price_legacy");
        when(getPrice.execute(id)).thenReturn(price);
        assertThatThrownBy(() -> new UpdatePriceUseCase(prices, getPrice, stripe).execute(id, new UpdatePriceDto(false)))
                .isInstanceOf(ProtectedFreePlanException.class);
        assertThat(price.isActive()).isTrue();
        verifyNoInteractions(prices, stripe);
    }

    @Test void preventsRenamingReservedCodeBeforeApplyingOtherEdits() {
        var stripe = mock(UpdateStripeProductUseCase.class);
        Plan plan = free(null);
        when(getPlan.execute(id)).thenReturn(plan);
        UpdatePlanDto dto = new UpdatePlanDto();
        dto.setCode("renamed"); dto.setName("Changed");
        assertThatThrownBy(() -> new UpdatePlanUseCase(plans, getPlan, stripe).execute(id, dto))
                .isInstanceOf(ProtectedFreePlanException.class);
        assertThat(plan.getCode()).isEqualTo(Plan.FREE_CODE);
        assertThat(plan.getName()).isEqualTo("Free");
        verifyNoInteractions(plans, stripe);
    }

    @Test void reservesFreeCodeWhenCreatingOrRenamingAnotherPlan() {
        var createStripe = mock(CreateStripeProductUseCase.class);
        CreatePlanDto create = new CreatePlanDto(); create.setCode(Plan.FREE_CODE);
        assertThatThrownBy(() -> new CreatePlanUseCase(plans, createStripe).execute(create))
                .isInstanceOf(ProtectedFreePlanException.class);
        var updateStripe = mock(UpdateStripeProductUseCase.class);
        Plan paid = new Plan(); paid.setCode("paid");
        when(getPlan.execute(id)).thenReturn(paid);
        UpdatePlanDto update = new UpdatePlanDto(); update.setCode(Plan.FREE_CODE);
        assertThatThrownBy(() -> new UpdatePlanUseCase(plans, getPlan, updateStripe).execute(id, update))
                .isInstanceOf(ProtectedFreePlanException.class);
        assertThat(paid.getCode()).isEqualTo("paid");
        verifyNoInteractions(plans, createStripe, updateStripe);
    }

    @Test void allowsFreeNameAndDescriptionEditsAndUnchangedCode() {
        var stripe = mock(UpdateStripeProductUseCase.class);
        Plan plan = free(null);
        when(getPlan.execute(id)).thenReturn(plan);
        when(plans.save(plan)).thenReturn(plan);
        UpdatePlanDto dto = new UpdatePlanDto();
        dto.setCode(Plan.FREE_CODE); dto.setName("Starter"); dto.setDescription("Updated description");
        Plan result = new UpdatePlanUseCase(plans, getPlan, stripe).execute(id, dto);
        assertThat(result.getName()).isEqualTo("Starter");
        assertThat(result.getDescription()).isEqualTo("Updated description");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getStatus()).isEqualTo(PlanStatus.PUBLISHED);
        verifyNoInteractions(stripe);
    }

    @Test void stillAllowsPaidPlanDeletion() {
        var stripe = mock(DeleteStripeProductUseCase.class);
        Plan paid = new Plan(); paid.setCode("paid"); paid.setStripePlanId("prod_paid");
        when(getPlan.execute(id)).thenReturn(paid);
        new DeletePlanUseCase(stripe, plans, getPlan).execute(id);
        verify(plans).delete(paid);
        verify(stripe).execute("prod_paid");
    }
}
