package com.jexis.jexis_backend.account.application.useCases;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;
import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.common.dtoHelpers.DtoHelper;
import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.stripe.application.useCases.CreateConnectUseCase;
import com.jexis.jexis_backend.stripe.application.useCases.CreateLinkUseCase;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.stripe.model.AccountLink;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CreateAccountBootstrapTest {
    @Mock AccountRepository accounts;
    @Mock AsyncLogger logger;
    @Mock CreateConnectUseCase connect;
    @Mock CreateLinkUseCase links;
    @Mock DtoHelper dto;
    @Mock MemberRepository members;
    @Mock GetUserUseCase users;
    @Mock com.jexis.jexis_backend.subscription.application.useCases.CreateFreeSubscriptionUseCase freeSubscription;
    @InjectMocks CreateAccountUseCase create;

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = false)
    void rejectsUnverifiedOwnersBeforeCreatingAnything(Boolean activated) {
        UUID userId = UUID.randomUUID();
        User owner = new User();
        owner.setIsActivated(activated);
        when(users.execute(userId)).thenReturn(owner);
        assertThatThrownBy(() -> create.execute(new AuthUser(userId, "Test", "test@example.com", true, List.of())))
                .isInstanceOf(com.jexis.jexis_backend.user.domain.exceptions.UserNotActivatedException.class);
        verifyNoInteractions(accounts, connect, links, members, freeSubscription);
    }

    @Test void createsInitialOwnerAndFreeSubscription() {
        UUID userId = UUID.randomUUID();
        User owner = new User(); owner.setId(userId); owner.setEmail("test@example.com"); owner.setIsActivated(true);
        when(users.execute(userId)).thenReturn(owner);
        var stripe = new com.stripe.model.Account(); stripe.setId("acct_test");
        when(connect.execute(owner.getEmail())).thenReturn(stripe);
        AccountLink link = new AccountLink(); link.setUrl("https://example.com/onboard");
        when(links.execute(eq("acct_test"), any())).thenReturn(link);
        when(accounts.save(any())).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0); account.setId(UUID.randomUUID()); return account;
        });
        create.execute(new AuthUser(userId, "Test", owner.getEmail(), true, List.of()));
        ArgumentCaptor<Member> member = ArgumentCaptor.forClass(Member.class);
        verify(members).save(member.capture());
        verify(freeSubscription).execute(member.getValue().getAccount());
        assertThat(member.getValue().getRole()).isEqualTo(Role.OWNER);
        assertThat(member.getValue().getUser()).isSameAs(owner);
        assertThat(member.getValue().getAccount().getOwner()).isSameAs(owner);
    }
}
