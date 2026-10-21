package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.stripe.application.useCases.GetStripePaymentMethodUseCase;
import com.jexis.jexis_backend.user.application.dto.PaymentMethodResponseDto;
import com.stripe.model.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPaymentMethodUseCase {
    private final GetStripePaymentMethodUseCase getStripePaymentMethodUseCase;

    public PaymentMethod execute(String customerId, String paymentMethodId) {
        return getStripePaymentMethodUseCase.execute(customerId, paymentMethodId);
    }
}
