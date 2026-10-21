package com.jexis.jexis_backend.account.presentation;

import com.jexis.jexis_backend.account.application.dto.AccountResourceLimitDto;
import com.jexis.jexis_backend.account.application.security.AccountAuthorization;
import com.jexis.jexis_backend.account.application.useCases.CheckAccountResourceLimitUseCase;
import com.jexis.jexis_backend.account.domain.enums.AccountResource;
import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.common.web.error.GlobalExceptionHandler;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountResourceLimitControllerTest {
    @TestConfiguration @EnableMethodSecurity
    static class Config {
        @Bean AccountAuthorization accountAuthorization() { return mock(AccountAuthorization.class); }
        @Bean CheckAccountResourceLimitUseCase checker() { return mock(CheckAccountResourceLimitUseCase.class); }
        @Bean AccountResourceLimitController controller(CheckAccountResourceLimitUseCase checker) {
            return new AccountResourceLimitController(checker);
        }
    }
    AnnotationConfigApplicationContext context;
    MockMvc mvc;
    CheckAccountResourceLimitUseCase checker;
    AccountAuthorization authorization;
    UUID accountId = UUID.randomUUID(), userId = UUID.randomUUID();

    @BeforeEach void setup() {
        context = new AnnotationConfigApplicationContext(Config.class);
        checker = context.getBean(CheckAccountResourceLimitUseCase.class);
        authorization = context.getBean(AccountAuthorization.class);
        mvc = MockMvcBuilders.standaloneSetup(context.getBean(AccountResourceLimitController.class))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(userId, "Name", "test@example.com", true, List.of()), null, List.of()));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); context.close(); }

    @Test void authorizedRequestReturnsDecisionWithDefaultQuantity() throws Exception {
        when(authorization.canView(userId, accountId)).thenReturn(true);
        when(checker.execute(accountId, AccountResource.CARDS, 1))
                .thenReturn(new AccountResourceLimitDto(true, 8, 10L, 2, 1, null));
        mvc.perform(get("/accounts/{id}/limits/cards", accountId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.limit").value(10)).andExpect(jsonPath("$.used").value(8));
    }
    @Test void walletsRouteUsesWalletResource() throws Exception {
        when(authorization.canView(userId, accountId)).thenReturn(true);
        when(checker.execute(accountId, AccountResource.WALLETS, 1))
                .thenReturn(new AccountResourceLimitDto(false, 2, 2L, 0, 1, "LIMIT_EXCEEDED"));
        mvc.perform(get("/accounts/{id}/limits/wallets", accountId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.allowed").value(false))
                .andExpect(jsonPath("$.reason").value("LIMIT_EXCEEDED"));
    }

    @Test void unrelatedAccountCannotInspectUsage() throws Exception {
        mvc.perform(get("/accounts/{id}/limits/members", accountId)).andExpect(status().isForbidden());
        verifyNoInteractions(checker);
    }
    @Test void invalidInputsReturn400() throws Exception {
        when(authorization.canView(userId, accountId)).thenReturn(true);
        for (String quantity : List.of("-1", "1.5", "abc", "9223372036854775808")) {
            mvc.perform(get("/accounts/{id}/limits/cards", accountId).param("additionalQuantity", quantity))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_QUANTITY"));
        }
        mvc.perform(get("/accounts/{id}/limits/unknown", accountId)).andExpect(status().isBadRequest());
        verifyNoInteractions(checker);
    }
}
