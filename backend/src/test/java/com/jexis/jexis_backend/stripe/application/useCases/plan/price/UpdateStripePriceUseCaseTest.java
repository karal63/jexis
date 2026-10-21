package com.jexis.jexis_backend.stripe.application.useCases.plan.price;

import com.jexis.jexis_backend.plan.application.dto.UpdatePriceDto;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.param.PriceUpdateParams;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateStripePriceUseCaseTest {

    @Mock
    private StripeClient stripeClient;

    @Mock
    private V1Services v1Services;

    @Mock
    private PriceService priceService;

    @InjectMocks
    private UpdateStripePriceUseCase updateStripePriceUseCase;

    @BeforeEach
    void setUp() {
        lenient().when(stripeClient.v1()).thenReturn(v1Services);
        lenient().when(v1Services.prices()).thenReturn(priceService);
    }

    @Test
    void execute_ShouldUpdatePriceInStripe() throws StripeException {
        UpdatePriceDto dto = new UpdatePriceDto(false);

        updateStripePriceUseCase.execute("price_stripe_123", dto);

        ArgumentCaptor<PriceUpdateParams> captor = ArgumentCaptor.forClass(PriceUpdateParams.class);
        verify(priceService).update(eq("price_stripe_123"), captor.capture());

        PriceUpdateParams params = captor.getValue();
        assertThat(params.getActive()).isFalse();
    }

    @Test
    void execute_WhenStripeFails_ShouldThrowRuntimeException() throws StripeException {
        UpdatePriceDto dto = new UpdatePriceDto(true);

        when(priceService.update(eq("price_stripe_123"), any(PriceUpdateParams.class)))
                .thenThrow(new com.stripe.exception.InvalidRequestException("Invalid", null, null, null, null, null));

        assertThatThrownBy(() -> updateStripePriceUseCase.execute("price_stripe_123", dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to update Stripe price");
    }
}
