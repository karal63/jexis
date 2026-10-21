package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.application.dto.CreatePriceDto;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.infrastructure.PriceRepository;
import com.jexis.jexis_backend.stripe.application.useCases.plan.price.CreateStripePriceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePriceUseCaseTest {

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private com.jexis.jexis_backend.plan.infrastructure.PlanRepository planRepository;

    @Mock
    private com.jexis.jexis_backend.stripe.application.useCases.plan.price.SetStripeDefaultPriceUseCase setStripeDefaultPriceUseCase;

    @Mock
    private GetPlanUseCase getPlanUseCase;

    @Mock
    private CreateStripePriceUseCase createStripePriceUseCase;

    @InjectMocks
    private CreatePriceUseCase createPriceUseCase;

    private Plan plan;
    private UUID planId;
    private com.stripe.model.Price stripePrice;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();
        plan = new Plan("prod_123", "Pro", "PRO", "Pro Plan", true, com.jexis.jexis_backend.plan.domain.enums.PlanStatus.DRAFT);
        plan.setId(planId);

        stripePrice = new com.stripe.model.Price();
        stripePrice.setId("price_stripe_123");
    }

    @Test
    void execute_WithPlanIdAndMonthlyInterval_ShouldCreateAndSavePrice() {
        CreatePriceDto dto = new CreatePriceDto("usd", 2900L, "monthly", 1, true);

        when(getPlanUseCase.execute(planId)).thenReturn(plan);
        when(createStripePriceUseCase.execute(eq("prod_123"), eq(dto))).thenReturn(stripePrice);
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Price result = createPriceUseCase.execute(planId, dto);

        assertThat(result).isNotNull();
        assertThat(result.getPlan()).isEqualTo(plan);
        assertThat(result.getStripePriceId()).isEqualTo("price_stripe_123");
        assertThat(result.getCurrency()).isEqualTo("usd");
        assertThat(result.getUnitAmount()).isEqualTo(2900L);
        assertThat(result.getInterval()).isEqualTo("month");
        assertThat(result.getIntervalCount()).isEqualTo(1);
        assertThat(result.isActive()).isTrue();

        verify(getPlanUseCase).execute(planId);
        verify(createStripePriceUseCase).execute("prod_123", dto);
        verify(priceRepository).save(any(Price.class));
    }

    @Test
    void execute_WithYearlyInterval_ShouldCreateAndSavePrice() {
        CreatePriceDto dto = new CreatePriceDto("usd", 29000L, "yearly", 1, true);

        when(getPlanUseCase.execute(planId)).thenReturn(plan);
        when(createStripePriceUseCase.execute(eq("prod_123"), eq(dto))).thenReturn(stripePrice);
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Price result = createPriceUseCase.execute(planId, dto);

        assertThat(result.getInterval()).isEqualTo("year");
        assertThat(result.getUnitAmount()).isEqualTo(29000L);
    }

    @Test
    void execute_WithDtoContainingPlanId_ShouldWork() {
        CreatePriceDto dto = new CreatePriceDto("usd", 1500L, "month", 1, true);

        when(getPlanUseCase.execute(planId)).thenReturn(plan);
        when(createStripePriceUseCase.execute(eq("prod_123"), eq(dto))).thenReturn(stripePrice);
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Price result = createPriceUseCase.execute(planId, dto);

        assertThat(result.getPlan()).isEqualTo(plan);
    }

    @Test
    void execute_WithInvalidInterval_ShouldThrowIllegalArgumentException() {
        CreatePriceDto dto = new CreatePriceDto("usd", 1500L, "daily", 1, true);

        when(getPlanUseCase.execute(planId)).thenReturn(plan);
        when(createStripePriceUseCase.execute(eq("prod_123"), eq(dto))).thenReturn(stripePrice);

        assertThatThrownBy(() -> createPriceUseCase.execute(planId, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported interval");
    }
}
