package com.jexis.jexis_backend.stripe.application.useCases.plan.price;

import com.jexis.jexis_backend.plan.application.dto.CreatePriceDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.param.PriceCreateParams;
import com.stripe.service.PriceService;
import com.stripe.service.V1Services;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateStripePriceUseCaseTest {

    @Mock
    private StripeClient stripeClient;

    @Mock
    private V1Services v1Services;

    @Mock
    private PriceService priceService;

    @InjectMocks
    private CreateStripePriceUseCase createStripePriceUseCase;

    @BeforeEach
    void setUp() {
        lenient().when(stripeClient.v1()).thenReturn(v1Services);
        lenient().when(v1Services.prices()).thenReturn(priceService);
    }

    @Test
    void execute_WhenMonthly_ShouldCallStripeWithMonthInterval() throws StripeException {
        CreatePriceDto dto = new CreatePriceDto("USD", 1500L, "monthly", 1, true);
        Price expectedPrice = new Price();
        expectedPrice.setId("price_stripe_123");

        when(priceService.create(any(PriceCreateParams.class))).thenReturn(expectedPrice);

        Price result = createStripePriceUseCase.execute("prod_123", dto);

        assertThat(result).isEqualTo(expectedPrice);

        ArgumentCaptor<PriceCreateParams> captor = ArgumentCaptor.forClass(PriceCreateParams.class);
        verify(priceService).create(captor.capture());

        PriceCreateParams params = captor.getValue();
        assertThat(params.getProduct()).isEqualTo("prod_123");
        assertThat(params.getCurrency()).isEqualTo("usd");
        assertThat(params.getUnitAmount()).isEqualTo(1500L);
    }

    @Test
    void execute_WhenYearly_ShouldCallStripeWithYearInterval() throws StripeException {
        CreatePriceDto dto = new CreatePriceDto("USD", 15000L, "yearly", 1, true);
        Price expectedPrice = new Price();
        expectedPrice.setId("price_stripe_456");

        when(priceService.create(any(PriceCreateParams.class))).thenReturn(expectedPrice);

        Price result = createStripePriceUseCase.execute("prod_123", dto);

        assertThat(result).isEqualTo(expectedPrice);
        verify(priceService).create(any(PriceCreateParams.class));
    }

    @Test
    void execute_WhenStripeFails_ShouldThrowRuntimeException() throws StripeException {
        CreatePriceDto dto = new CreatePriceDto("USD", 1500L, "monthly", 1, true);

        when(priceService.create(any(PriceCreateParams.class)))
                .thenThrow(new com.stripe.exception.InvalidRequestException("Invalid", null, null, null, null, null));

        assertThatThrownBy(() -> createStripePriceUseCase.execute("prod_123", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create Stripe price");
    }
}
