package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.domain.exceptions.PriceNotFoundException;
import com.jexis.jexis_backend.plan.infrastructure.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPriceUseCaseTest {

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private GetPlanPriceUseCase getPlanPriceUseCase;

    private UUID priceId;
    private Price price;

    @BeforeEach
    void setUp() {
        priceId = UUID.randomUUID();
        Plan plan = new Plan("prod_123", "Basic", "BASIC", "Description", true, com.jexis.jexis_backend.plan.domain.enums.PlanStatus.DRAFT);
        price = new Price(plan, "price_stripe_123", "usd", 1000L, "month", 1, true);
        price.setId(priceId);
    }

    @Test
    void execute_WhenPriceExists_ShouldReturnPrice() {
        when(priceRepository.findById(priceId)).thenReturn(Optional.of(price));

        Price result = getPlanPriceUseCase.execute(priceId);

        assertThat(result).isEqualTo(price);
        verify(priceRepository).findById(priceId);
    }

    @Test
    void execute_WhenPriceDoesNotExist_ShouldThrowPriceNotFoundException() {
        when(priceRepository.findById(priceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPlanPriceUseCase.execute(priceId))
                .isInstanceOf(PriceNotFoundException.class);
    }
}
