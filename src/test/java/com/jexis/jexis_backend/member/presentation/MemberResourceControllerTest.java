package com.jexis.jexis_backend.member.presentation;

import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.card.application.useCases.GetMemberCardsUseCase;
import com.jexis.jexis_backend.transaction.application.useCases.GetMemberTransactionsUseCase;
import com.jexis.jexis_backend.authorization.application.useCases.GetMemberAuthorizationsUseCase;
import com.jexis.jexis_backend.dispute.application.useCases.GetMemberDisputesUseCase;
import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.common.web.error.GlobalExceptionHandler;
import com.jexis.jexis_backend.member.application.security.MemberResourceAuthorization;
import com.jexis.jexis_backend.member.application.useCases.*;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.application.security.UserAuthorization;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberResourceControllerTest {
    @Configuration @EnableMethodSecurity
    static class Config {
        @Bean MemberRepository memberRepository() { return mock(MemberRepository.class); }
        @Bean GetMemberUseCase getMemberUseCase(MemberRepository repo) { return new GetMemberUseCase(repo); }
        @Bean HasRoleUseCase hasRoleUseCase() { return mock(HasRoleUseCase.class); }
        @Bean UserAuthorization userAuthorization() { return mock(UserAuthorization.class); }
        @Bean MemberResourceAuthorization memberResourceAuthorization(GetMemberUseCase members, HasRoleUseCase roles, UserAuthorization users) {
            return new MemberResourceAuthorization(members, roles, users);
        }
        @Bean GetMemberCardsUseCase cards() { return mock(GetMemberCardsUseCase.class); }
        @Bean GetMemberTransactionsUseCase transactions() { return mock(GetMemberTransactionsUseCase.class); }
        @Bean GetMemberAuthorizationsUseCase authorizations() { return mock(GetMemberAuthorizationsUseCase.class); }
        @Bean GetMemberDisputesUseCase disputes() { return mock(GetMemberDisputesUseCase.class); }
        @Bean DtoHelper dtoHelper() { return mock(DtoHelper.class); }
        @Bean MemberResourceController controller(DtoHelper dto, GetMemberCardsUseCase cards, GetMemberTransactionsUseCase transactions,
                GetMemberAuthorizationsUseCase authorizations, GetMemberDisputesUseCase disputes) {
            return new MemberResourceController(dto, cards, transactions, authorizations, disputes);
        }
    }
    AnnotationConfigApplicationContext context;
    MockMvc mvc;
    UUID accountId = UUID.randomUUID(), memberId = UUID.randomUUID(), userId = UUID.randomUUID(), requesterId = UUID.randomUUID();
    MemberRepository members;
    HasRoleUseCase roles;
    UserAuthorization users;
    GetMemberCardsUseCase cards;
    GetMemberTransactionsUseCase transactions;
    GetMemberAuthorizationsUseCase authorizations;
    GetMemberDisputesUseCase disputes;

    @BeforeEach void setup() {
        context = new AnnotationConfigApplicationContext(Config.class);
        members = context.getBean(MemberRepository.class); roles = context.getBean(HasRoleUseCase.class);
        users = context.getBean(UserAuthorization.class); cards = context.getBean(GetMemberCardsUseCase.class);
        transactions = context.getBean(GetMemberTransactionsUseCase.class);
        authorizations = context.getBean(GetMemberAuthorizationsUseCase.class); disputes = context.getBean(GetMemberDisputesUseCase.class);
        Account account = new Account(); account.setId(accountId);
        User user = new User(); user.setId(userId);
        Member member = new Member(account, user, Role.MEMBER); member.setId(memberId);
        when(members.findById(memberId)).thenReturn(Optional.of(member));
        when(cards.execute(eq(accountId), eq(memberId), anyInt(), anyInt(), any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());
        when(transactions.execute(eq(accountId), eq(memberId), anyInt(), anyInt(), any(), any(), any(), any(), any(), any())).thenReturn(Page.empty());
        when(authorizations.execute(eq(accountId), eq(memberId), anyInt(), anyInt(), any(), any(), any(), any(), any())).thenReturn(Page.empty());
        when(disputes.execute(eq(accountId), eq(memberId), anyInt(), anyInt(), any(), any(), any(), any(), any())).thenReturn(Page.empty());
        mvc = MockMvcBuilders.standaloneSetup(context.getBean(MemberResourceController.class))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        login(requesterId);
    }
    void login(UUID id) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthUser(id, "Test", "test@example.com", true, List.of()), null, List.of()));
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); context.close(); }

    @ParameterizedTest @ValueSource(strings = {"cards", "transactions", "authorizations", "disputes"})
    void selfCompanyAdminOwnerAndGlobalAdminCanRead(String resource) throws Exception {
        String path = "/accounts/{accountId}/members/{memberId}/" + resource;
        login(userId);
        mvc.perform(get(path, accountId, memberId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1)).andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.items").isEmpty());
        login(requesterId);
        for (Role role : List.of(Role.ADMIN, Role.OWNER)) {
            reset(roles);
            when(roles.execute(requesterId, accountId, role)).thenReturn(true);
            mvc.perform(get(path, accountId, memberId)).andExpect(status().isOk());
        }
        reset(roles);
        when(users.isAdmin(requesterId)).thenReturn(true);
        mvc.perform(get(path, accountId, memberId)).andExpect(status().isOk());
    }

    @ParameterizedTest @ValueSource(strings = {"cards", "transactions", "authorizations", "disputes"})
    void ordinaryUsersCannotReadAnotherMembersRecords(String resource) throws Exception {
        mvc.perform(get("/accounts/{a}/members/{m}/" + resource, accountId, memberId)).andExpect(status().isForbidden());
        verifyNoInteractions(cards, transactions, authorizations, disputes);
    }

    @ParameterizedTest @ValueSource(strings = {"cards", "transactions", "authorizations", "disputes"})
    void wrongCompanyOrMissingMemberCannotReadEvenAsGlobalAdmin(String resource) throws Exception {
        when(users.isAdmin(requesterId)).thenReturn(true);
        mvc.perform(get("/accounts/{a}/members/{m}/" + resource, UUID.randomUUID(), memberId)).andExpect(status().isNotFound());
        mvc.perform(get("/accounts/{a}/members/{m}/" + resource, accountId, UUID.randomUUID())).andExpect(status().isNotFound());
        verifyNoInteractions(cards, transactions, authorizations, disputes);
    }

    @ParameterizedTest @ValueSource(strings = {"cards", "transactions", "authorizations", "disputes"})
    void invalidPaginationReturns400WithoutReadingRecords(String resource) throws Exception {
        login(userId);
        String path = "/accounts/{a}/members/{m}/" + resource;
        mvc.perform(get(path, accountId, memberId).param("page", "0")).andExpect(status().isBadRequest());
        mvc.perform(get(path, accountId, memberId).param("pageSize", "0")).andExpect(status().isBadRequest());
        mvc.perform(get(path, accountId, memberId).param("pageSize", "101")).andExpect(status().isBadRequest());
        verifyNoInteractions(cards, transactions, authorizations, disputes);
    }
}
