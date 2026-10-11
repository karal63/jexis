package com.jexis.jexis_backend.authorization.presentation;

import com.jexis.jexis_backend.account.application.useCases.*;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.authorization.application.useCases.*;
import com.jexis.jexis_backend.card.application.useCases.GetCardByStripeIdUseCase;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.card.domain.exceptions.CardNotFoundException;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.cardholder.infrastructure.CardHolderRepository;
import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.subscription.application.useCases.GetEffectiveSubscriptionEntitlementsUseCase;
import com.jexis.jexis_backend.subscription.domain.entities.Subscription;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.wallet.application.useCases.SyncBalanceUseCase;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;
import com.stripe.Stripe;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorizationWebhookControllerTest {
    private static final String SECRET = "whsec_test";
    @Mock GetCardByStripeIdUseCase cards;
    @Mock SubscriptionRepository subscriptions;
    @Mock GetEffectiveSubscriptionEntitlementsUseCase entitlements;
    @Mock CardRepository cardRepository;
    @Mock CreateAuthorizationUseCase createAuthorization;
    @Mock UpdateAuthorizationUseCase updateAuthorization;
    private AuthorizationWebhookController controller;
    private final UUID accountId = UUID.randomUUID();
    private Card card;

    @BeforeEach
    void setup() {
        var checker = new CheckAccountResourceLimitUseCase(mock(GetAccountUseCase.class), subscriptions,
                entitlements, cardRepository, mock(MemberRepository.class), mock(CardHolderRepository.class),
                mock(WalletRepository.class));
        controller = new AuthorizationWebhookController(mock(AsyncLogger.class), createAuthorization,
                updateAuthorization, mock(SyncBalanceUseCase.class),
                new CheckAuthorizationEligibilityUseCase(cards, checker));
        ReflectionTestUtils.setField(controller, "webhookSecret", SECRET);
        var account = mock(Account.class);
        lenient().when(account.getId()).thenReturn(accountId);
        var holder = new CardHolder();
        holder.setAccount(account);
        var wallet = new Wallet("Test", "fa_test", account);
        card = new Card();
        card.setCardHolder(holder);
        card.setTreasuryAccount(wallet);
        card.setStatus(CardStatus.active);
    }

    @Test
    void approvesEligibleSubscriptionWithoutRecheckingCreationCapacity() throws Exception {
        when(cards.execute("ic_test")).thenReturn(card);
        when(subscriptions.findEligibleForResourceLimits(eq(accountId), any(),
                eq(List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING))))
                .thenReturn(List.of(new Subscription()));
        assertDecision(request("{\"id\":\"ic_test\"}"), true);
        verifyNoInteractions(entitlements, cardRepository, createAuthorization, updateAuthorization);
    }

    @Test
    void declinesMissingSubscriptionAndResumesAfterRecovery() throws Exception {
        when(cards.execute("ic_test")).thenReturn(card);
        when(subscriptions.findEligibleForResourceLimits(eq(accountId), any(), any()))
                .thenReturn(List.of(), List.of(new Subscription()));
        assertDecision(request("{\"id\":\"ic_test\"}"), false);
        assertDecision(request("{\"id\":\"ic_test\"}"), true);
    }

    @Test
    void declinesAmbiguousSubscriptions() throws Exception {
        when(cards.execute("ic_test")).thenReturn(card);
        when(subscriptions.findEligibleForResourceLimits(eq(accountId), any(), any()))
                .thenReturn(List.of(new Subscription(), new Subscription()));
        assertDecision(request("{\"id\":\"ic_test\"}"), false);
    }

    @Test
    void declinesUnknownCard() throws Exception {
        when(cards.execute("ic_test")).thenThrow(new CardNotFoundException());
        assertDecision(request("{\"id\":\"ic_test\"}"), false);
        verifyNoInteractions(subscriptions);
    }

    @Test
    void declinesDatabaseFailureExplicitly() throws Exception {
        when(cards.execute("ic_test")).thenReturn(card);
        when(subscriptions.findEligibleForResourceLimits(eq(accountId), any(), any()))
                .thenThrow(new IllegalStateException("Database unavailable"));
        assertDecision(request("{\"id\":\"ic_test\"}"), false);
    }

    @ParameterizedTest
    @ValueSource(strings = {"inactive", "canceled", "deleted", "replacing", "different_account"})
    void declinesUnusableCards(String state) throws Exception {
        switch (state) {
            case "inactive" -> card.setStatus(CardStatus.inactive);
            case "canceled" -> card.setStatus(CardStatus.canceled);
            case "deleted" -> card.setIsDeleted(true);
            case "replacing" -> card.setReplacementPending(true);
            case "different_account" -> {
                Account otherAccount = mock(Account.class);
                when(otherAccount.getId()).thenReturn(UUID.randomUUID());
                card.getTreasuryAccount().setAccount(otherAccount);
            }
        }
        when(cards.execute("ic_test")).thenReturn(card);
        assertDecision(request("{\"id\":\"ic_test\"}"), false);
        verifyNoInteractions(subscriptions);
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "{}", "{\"id\":\"\"}"})
    void declinesMissingCardData(String json) throws Exception {
        assertDecision(request(json), false);
        verifyNoInteractions(cards, subscriptions);
    }

    @Test
    void declinesUndeserializableSignedEvent() throws Exception {
        assertDecision(request("{\"id\":\"ic_test\"}").replace(Stripe.API_VERSION, "2017-01-27"), false);
        verifyNoInteractions(cards, subscriptions);
    }

    @Test
    void rejectsInvalidSignatureBeforeCheckingEligibility() {
        var response = controller.handleWebhook(request("{\"id\":\"ic_test\"}"), "t=0,v1=invalid");
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        verifyNoInteractions(cards, subscriptions);
    }

    private String request(String cardJson) {
        return "{\"id\":\"evt_test\",\"object\":\"event\",\"api_version\":\"" + Stripe.API_VERSION
                + "\",\"type\":\"issuing_authorization.request\",\"data\":{\"object\":{"
                + "\"id\":\"iauth_test\",\"object\":\"issuing.authorization\",\"card\":" + cardJson + "}}}";
    }

    private void assertDecision(String payload, boolean approved) throws Exception {
        long timestamp = Instant.now().getEpochSecond();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String signature = HexFormat.of().formatHex(mac.doFinal((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8)));
        var response = controller.handleWebhook(payload, "t=" + timestamp + ",v1=" + signature);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getHeaders().getFirst("Stripe-Version")).isEqualTo("2026-06-24.dahlia");
        assertThat(response.getBody()).isEqualTo(Map.of("approved", approved));
    }
}
