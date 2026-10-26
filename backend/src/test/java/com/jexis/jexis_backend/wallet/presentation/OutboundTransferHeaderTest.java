package com.jexis.jexis_backend.wallet.presentation;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.jexis.jexis_backend.common.web.error.GlobalExceptionHandler;
import com.jexis.jexis_backend.wallet.application.useCases.CreateOutboundTransferUseCase;

class OutboundTransferHeaderTest {
    @Test void missingAndInvalidKeysAreBadRequestsAndValidKeyReachesService() throws Exception {
        var service = mock(CreateOutboundTransferUseCase.class);
        var controller = new WalletController(null, null, null, null, null, null, null, null, null, service, null);
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        UUID wallet = UUID.randomUUID(), key = UUID.randomUUID();
        String path = "/wallets/" + wallet + "/create-outbound-transfer";
        String body = "{\"amount\":100,\"currency\":\"usd\",\"description\":\"Test\",\"externalAccountId\":\"" + UUID.randomUUID() + "\"}";
        mvc.perform(post(path).contentType("application/json").content(body)).andExpect(status().isBadRequest());
        mvc.perform(post(path).header("Idempotency-Key", "invalid").contentType("application/json").content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
        mvc.perform(post(path).header("Idempotency-Key", key).contentType("application/json").content(body))
                .andExpect(status().isOk());
        verify(service).execute(eq(wallet), any(), eq(key));
    }
}
