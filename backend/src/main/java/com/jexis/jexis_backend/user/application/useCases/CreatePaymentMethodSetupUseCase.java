package com.jexis.jexis_backend.user.application.useCases;

import com.jexis.jexis_backend.stripe.application.useCases.CreateStripeCustomerUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.CreateStripeCustomerPortalSessionUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import com.stripe.model.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreatePaymentMethodSetupUseCase {
    private final GetUserUseCase getUserUseCase;
    private final CreateStripeCustomerUseCase createStripeCustomerUseCase;
    private final CreateStripeCustomerPortalSessionUseCase createStripeCustomerPortalSessionUseCase;
    private final UserRepository userRepository;

    public String execute(UUID userId) {
        User user = getUserUseCase.execute(userId);

        if (user.getStripeCustomerId() == null) {
            Customer customer = createStripeCustomerUseCase.execute(user);
            user.setStripeCustomerId(customer.getId());
            userRepository.save(user);
        }

        return createStripeCustomerPortalSessionUseCase.execute(user.getStripeCustomerId());
    }
}
