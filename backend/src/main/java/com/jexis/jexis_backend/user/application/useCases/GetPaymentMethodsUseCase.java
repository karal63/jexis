package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.stripe.application.useCases.GetStripePaymentMethodsUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.stripe.model.PaymentMethod;
import com.stripe.model.StripeCollection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPaymentMethodsUseCase {
    private final GetUserUseCase getUserUseCase;
    private final GetStripePaymentMethodsUseCase getStripePaymentMethodsUseCase;

    public List<PaymentMethod> execute(UUID userId) {
        User user = getUserUseCase.execute(userId);
        StripeCollection<PaymentMethod> paymentMethods = getStripePaymentMethodsUseCase.execute(user.getStripeCustomerId());

        return paymentMethods.getData();
    }
}
