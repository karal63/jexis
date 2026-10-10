package com.jexis.jexis_backend.subscription.presentation;

import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.subscription.application.useCases.GetActiveAccountSubscriptionUseCase;
import com.jexis.jexis_backend.subscription.domain.exceptions.SubscriptionNotFoundException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {
    @Mock
    private GetActiveAccountSubscriptionUseCase getActiveAccountSubscriptionUseCase;
    @Mock
    private DtoHelper dtoHelper;
    @InjectMocks
    private SubscriptionController controller;

    @Test
    void missingActiveSubscriptionRaisesDomainNotFoundBeforeMapping() {
        assertThatThrownBy(() -> controller.getAccountSubscription(UUID.randomUUID()))
                .isInstanceOf(SubscriptionNotFoundException.class);
        verifyNoInteractions(dtoHelper);
    }
}
