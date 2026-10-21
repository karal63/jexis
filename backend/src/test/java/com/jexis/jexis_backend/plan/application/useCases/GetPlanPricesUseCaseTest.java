package com.jexis.jexis_backend.plan.application.useCases;

import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.entities.Price;
import com.jexis.jexis_backend.plan.infrastructure.PriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPlanPricesUseCaseTest {

    @Mock
    private PriceRepository priceRepository;

    @InjectMocks
    private GetPlanPricesUseCase getPlanPricesUseCase;

    private UUID planId;
    private Price price1;
    private Price price2;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();
        Plan plan = new Plan("prod_123", "Basic", "BASIC", "Description", true, com.jexis.jexis_backend.plan.domain.enums.PlanStatus.DRAFT);
        plan.setId(planId);

        price1 = new Price(plan, "price_1", "usd", 1000L, "month", 1, true);
        price2 = new Price(plan, "price_2", "usd", 10000L, "year", 1, true);
    }

    @Test
    void execute_ShouldReturnAllPricesForPlan() {
        when(priceRepository.findAllByPlanId(planId)).thenReturn(List.of(price1, price2));

        List<Price> result = getPlanPricesUseCase.execute(planId);

        assertThat(result).containsExactly(price1, price2);
        verify(priceRepository).findAllByPlanId(planId);
    }
}
