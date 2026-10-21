package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.application.dto.UpdatePriceDto;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.domain.exceptions.PriceNotFoundException;
import com.jexis.jexis_backend.plan.infrastructure.PriceRepository;
import com.jexis.jexis_backend.stripe.application.useCases.plan.price.UpdateStripePriceUseCase;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePriceUseCaseTest {

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private GetPlanPriceUseCase getPlanPriceUseCase;

    @Mock
    private UpdateStripePriceUseCase updateStripePriceUseCase;

    @InjectMocks
    private UpdatePriceUseCase updatePriceUseCase;

    private Plan plan;
    private UUID priceId;
    private Price price;

    @BeforeEach
    void setUp() {
        plan = new Plan("prod_123", "Basic", "BASIC", "Description", true, com.jexis.jexis_backend.plan.domain.enums.PlanStatus.DRAFT);
        priceId = UUID.randomUUID();
        price = new Price(plan, "price_stripe_123", "usd", 1000L, "month", 1, true);
        price.setId(priceId);
    }

    @Test
    void execute_WhenPriceExists_ShouldUpdateActiveAndStripe() {
        UpdatePriceDto dto = new UpdatePriceDto(false);

        when(getPlanPriceUseCase.execute(priceId)).thenReturn(price);
        when(priceRepository.save(any(Price.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Price result = updatePriceUseCase.execute(priceId, dto);

        assertThat(result.isActive()).isFalse();
        verify(getPlanPriceUseCase).execute(priceId);
        verify(updateStripePriceUseCase).execute("price_stripe_123", dto);
        verify(priceRepository).save(price);
    }

    @Test
    void execute_WhenPriceNotFound_ShouldPropagateException() {
        UpdatePriceDto dto = new UpdatePriceDto(false);

        when(getPlanPriceUseCase.execute(priceId)).thenThrow(new PriceNotFoundException());

        assertThatThrownBy(() -> updatePriceUseCase.execute(priceId, dto))
                .isInstanceOf(PriceNotFoundException.class);

        verify(updateStripePriceUseCase, never()).execute(any(), any());
        verify(priceRepository, never()).save(any());
    }
}
