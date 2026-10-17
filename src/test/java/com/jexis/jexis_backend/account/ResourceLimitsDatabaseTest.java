package com.jexis.jexis_backend.account;

import com.jexis.jexis_backend.account.application.useCases.*;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.domain.enums.AccountResource;
import com.jexis.jexis_backend.account.domain.exception.ResourceLimitException;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;
import com.jexis.jexis_backend.card.application.useCases.*;
import com.jexis.jexis_backend.card.application.dto.ReplaceCardDto;
import com.jexis.jexis_backend.card.application.dto.CreateCardDto;
import com.jexis.jexis_backend.card.application.dto.EditCardDto;
import com.jexis.jexis_backend.cardholder.application.useCases.GetCardHolderUseCase;
import com.jexis.jexis_backend.cardholder.application.useCases.CreateCardHolderUseCase;
import com.jexis.jexis_backend.cardholder.application.dto.CreateCardHolderDto;
import com.jexis.jexis_backend.common.logging.AsyncLogger;
import com.jexis.jexis_backend.cardholder.infrastructure.CardHolderRepository;
import com.jexis.jexis_backend.wallet.application.useCases.GetWalletUseCase;
import com.jexis.jexis_backend.wallet.infrastructure.WalletRepository;
import com.jexis.jexis_backend.member.application.useCases.CanAccessUseCase;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.enums.*;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.cardholder.domain.enums.CardHolderStatus;
import com.jexis.jexis_backend.entitlement.domain.entities.Entitlement;
import com.jexis.jexis_backend.entitlement.infrastructure.EntitlementRepository;
import com.jexis.jexis_backend.member.application.dto.CreateMemberDto;
import com.jexis.jexis_backend.member.application.useCases.AddMemberUseCase;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.plan.domain.entities.*;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import com.jexis.jexis_backend.plan.infrastructure.PlanEntitlementRepository;
import com.jexis.jexis_backend.stripe.application.useCases.*;
import com.jexis.jexis_backend.subscription.application.useCases.*;
import com.jexis.jexis_backend.subscription.domain.entities.*;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionStatus;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.user.application.useCases.GetUserUseCase;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.user.infrastructure.UserRepository;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import com.jexis.jexis_backend.wallet.application.useCases.CreateWalletUseCase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ResourceLimitsDatabaseTest.Config.class)
class ResourceLimitsDatabaseTest {
    @TestConfiguration @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {AccountRepository.class, CardRepository.class,
            MemberRepository.class, SubscriptionRepository.class, PlanEntitlementRepository.class,
            EntitlementRepository.class, UserRepository.class, CardHolderRepository.class, WalletRepository.class})
    @Import({LockAccountUseCase.class, GetAccountUseCase.class, GetUserUseCase.class,
            GetSubscriptionUseCase.class, GetEffectiveSubscriptionEntitlementsUseCase.class,
            CheckAccountResourceLimitUseCase.class, AddMemberUseCase.class, GetLockedCardUseCase.class,
            CardReplacementTransactions.class, ReplaceCardUseCase.class, CreateCardUseCase.class,
            GetCardHolderUseCase.class, GetWalletUseCase.class, CanAccessUseCase.class,
            GetCardUseCase.class, EditCardUseCase.class, DeleteCardUseCase.class, CreateCardHolderUseCase.class,
            CreateWalletUseCase.class})
    static class Config {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:limits;MODE=PostgreSQL;NON_KEYWORDS=KEY,VALUE,INTERVAL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000", "sa", "");
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(ds);
            factory.setPackagesToScan("com.jexis.jexis_backend");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory emf) { return new JpaTransactionManager(emf); }
        @Bean CreateStripeCardUseCase stripeCreate() { return mock(CreateStripeCardUseCase.class); }
        @Bean EditCardStatusUseCase stripeEdit() { return mock(EditCardStatusUseCase.class); }
        @Bean SetCardLimitsUseCase stripeLimits() { return mock(SetCardLimitsUseCase.class); }
        @Bean CreateStripeHolderUseCase stripeHolder() { return mock(CreateStripeHolderUseCase.class); }
        @Bean AsyncLogger logger() { return mock(AsyncLogger.class); }
        @Bean CreateTreasuryAccount stripeTreasury() { return mock(CreateTreasuryAccount.class); }
    }
    @PersistenceContext EntityManager em;
    @Autowired PlatformTransactionManager manager;
    @Autowired CheckAccountResourceLimitUseCase checker;
    @Autowired AddMemberUseCase addMember;
    @Autowired SubscriptionRepository subscriptions;
    @Autowired CardRepository cards;
    @Autowired ReplaceCardUseCase replaceCard;
    @Autowired CreateCardUseCase createCard;
    @Autowired EditCardUseCase editCard;
    @Autowired DeleteCardUseCase deleteCard;
    @Autowired CreateStripeCardUseCase stripeCreate;
    @Autowired EditCardStatusUseCase stripeEdit;
    @Autowired CreateCardHolderUseCase createHolder;
    @Autowired CreateStripeHolderUseCase stripeHolder;
    @Autowired CreateTreasuryAccount stripeTreasury;
    @Autowired CreateWalletUseCase createWallet;
    TransactionTemplate tx;
    UUID accountId, userId, subscriptionId;
    static UUID cardEntitlementId, memberEntitlementId;

    @BeforeEach void setup() {
        tx = new TransactionTemplate(manager);
        reset(stripeCreate, stripeEdit, stripeHolder, stripeTreasury);
        tx.executeWithoutResult(s -> {
            User owner = user();
            Account account = new Account("account@example.com", UUID.randomUUID().toString(), UUID.randomUUID().toString(), owner);
            em.persist(account);
            accountId = account.getId();
            userId = owner.getId();
            em.persist(new Member(account, owner, Role.OWNER));
            Plan plan = new Plan(UUID.randomUUID().toString(), "Test", UUID.randomUUID().toString(), null, true, PlanStatus.PUBLISHED);
            em.persist(plan);
            Price price = new Price(plan, UUID.randomUUID().toString(), "usd", 100L, "month", 1, true);
            em.persist(price);
            Subscription sub = new Subscription(owner, account, plan, price, UUID.randomUUID().toString(),
                    SubscriptionStatus.ACTIVE, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), false);
            em.persist(sub);
            subscriptionId = sub.getId();
            if (cardEntitlementId == null) {
                Entitlement ce = new Entitlement("max_cards", "NUMERIC", null);
                Entitlement me = new Entitlement("max_members", "NUMERIC", null);
                em.persist(ce); em.persist(me);
                cardEntitlementId = ce.getId(); memberEntitlementId = me.getId();
            }
            em.persist(new PlanEntitlement(plan, em.find(Entitlement.class, cardEntitlementId), "5"));
            em.persist(new PlanEntitlement(plan, em.find(Entitlement.class, memberEntitlementId), "2"));
        });
    }
    @Test void localSubscriptionWithoutExpirationRemainsEligible() {
        tx.executeWithoutResult(s -> {
            Subscription subscription = em.find(Subscription.class, subscriptionId);
            subscription.setStripeSubscriptionId(null);
            subscription.setCurrentPeriodEnd(null);
        });
        assertThat(subscriptions.findActiveSubscription(accountId, LocalDateTime.now().plusYears(100)))
                .isPresent();
        assertThat(checker.execute(accountId, AccountResource.MEMBERS, 1).allowed()).isTrue();
    }

    @Test void cardholdersUsePlanMemberLimitAndExcludeDeletedAndOtherAccounts() {
        tx.executeWithoutResult(s -> {
            Account account = em.find(Account.class, accountId);
            holder(account, account.getOwner(), CardHolderStatus.active, false);
            holder(account, user(), CardHolderStatus.inactive, false);
            holder(account, user(), CardHolderStatus.active, true);
            Account other = new Account("other@example.com", UUID.randomUUID().toString(), UUID.randomUUID().toString(), account.getOwner());
            em.persist(other);
            holder(other, account.getOwner(), CardHolderStatus.active, false);
        });
        var result = checker.execute(accountId, AccountResource.CARDHOLDERS, 1);
        assertThat(result.used()).isEqualTo(2);
        assertThat(result.limit()).isEqualTo(2);
        assertThat(result.reason()).isEqualTo("LIMIT_EXCEEDED");
        assertThat(checker.execute(accountId, AccountResource.MEMBERS, 1).allowed()).isTrue();
    }

    @Test void concurrentCardholderCreationAllowsThirtiethButDeniesThirtyFirstBeforeStripe() throws Exception {
        List<UUID> candidates = tx.execute(s -> {
            Account account = em.find(Account.class, accountId);
            for (int i = 0; i < 29; i++) holder(account, user(), CardHolderStatus.active, false);
            User first = user(), second = user();
            em.persist(new Member(account, first, Role.ADMIN));
            em.persist(new Member(account, second, Role.ADMIN));
            em.persist(new SubscriptionEntitlement(em.find(Subscription.class, subscriptionId),
                    em.find(Entitlement.class, memberEntitlementId), "30"));
            return List.of(first.getId(), second.getId());
        });
        when(stripeHolder.execute(any(), any())).thenAnswer(invocation -> {
            var holder = new com.stripe.model.issuing.Cardholder();
            holder.setId(UUID.randomUUID().toString()); holder.setStatus("active");
            return holder;
        });
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (UUID candidate : candidates) results.add(executor.submit(() -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("start timed out");
                try {
                    createHolder.execute(new CreateCardHolderDto(accountId, candidate, "Street", "City", "State", "US", "12345"),
                            new org.springframework.mock.web.MockHttpServletRequest());
                    return true;
                } catch (ResourceLimitException ex) {
                    assertThat(ex.getCode()).isEqualTo("LIMIT_EXCEEDED");
                    assertThat(ex.getStatus()).isEqualTo(403);
                    return false;
                }
            }));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue(); start.countDown();
            assertThat(List.of(results.get(0).get(15, TimeUnit.SECONDS), results.get(1).get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            assertThat(checker.execute(accountId, AccountResource.CARDHOLDERS, 0).used()).isEqualTo(30);
            verify(stripeHolder, times(1)).execute(any(), any());
        } finally { executor.shutdownNow(); }
    }

    CardHolder holder(Account account, User user, CardHolderStatus status, boolean deleted) {
        CardHolder holder = new CardHolder(UUID.randomUUID().toString(), account, user,
                "Test", "Street", "City", "State", "US", "12345", status);
        holder.setIsDeleted(deleted);
        em.persist(holder);
        return holder;
    }

    @Test void walletLimitsApplyOverridesAndSerializeCreationBeforeStripe() throws Exception {
        Account account = tx.execute(s -> em.find(Account.class, accountId));
        assertThatThrownBy(() -> createWallet.execute(account, "Missing limit"))
                .isInstanceOf(ResourceLimitException.class);
        verifyNoInteractions(stripeTreasury);
        UUID entitlementId = tx.execute(s -> {
            Account current = em.find(Account.class, accountId);
            Entitlement entitlement = new Entitlement("max_wallets", "limit", null);
            em.persist(entitlement);
            em.persist(new PlanEntitlement(em.find(Subscription.class, subscriptionId).getPlan(), entitlement, "2"));
            em.persist(new Wallet("Existing", "fa_" + UUID.randomUUID(), current));
            Wallet deleted = new Wallet("Deleted", "fa_" + UUID.randomUUID(), current);
            deleted.setIsDeleted(true); em.persist(deleted);
            Account other = new Account("other@example.com", UUID.randomUUID().toString(), UUID.randomUUID().toString(), current.getOwner());
            em.persist(other); em.persist(new Wallet("Other account", "fa_" + UUID.randomUUID(), other));
            return entitlement.getId();
        });
        var decision = checker.execute(accountId, AccountResource.WALLETS, 1);
        assertThat(decision.used()).isEqualTo(1);
        assertThat(decision.limit()).isEqualTo(2);
        assertThat(decision.allowed()).isTrue();
        tx.executeWithoutResult(s -> em.persist(new SubscriptionEntitlement(em.find(Subscription.class, subscriptionId),
                em.find(Entitlement.class, entitlementId), "1")));
        assertThatThrownBy(() -> createWallet.execute(account, "Overridden limit"))
                .isInstanceOf(ResourceLimitException.class);
        verifyNoInteractions(stripeTreasury);
        tx.executeWithoutResult(s -> em.createQuery("update SubscriptionEntitlement e set e.value = '2' where e.subscription.id = :id")
                .setParameter("id", subscriptionId).executeUpdate());
        when(stripeTreasury.execute(any(), any())).thenAnswer(invocation -> {
            var financialAccount = new com.stripe.model.treasury.FinancialAccount();
            financialAccount.setId("fa_" + UUID.randomUUID()); return financialAccount;
        });
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        Callable<Boolean> add = () -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("start timed out");
            try { createWallet.execute(account, "New wallet"); return true; }
            catch (ResourceLimitException ex) {
                assertThat(ex.getCode()).isEqualTo("LIMIT_EXCEEDED");
                assertThat(ex.getStatus()).isEqualTo(403); return false;
            }
        };
        try {
            Future<Boolean> first = executor.submit(add), second = executor.submit(add);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue(); start.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            verify(stripeTreasury, times(1)).execute(account.getConnectAccountId(), "New wallet");
            assertThat(checker.execute(accountId, AccountResource.WALLETS, 0).used()).isEqualTo(2);
        } finally { executor.shutdownNow(); }
    }

    User user() {
        String unique = UUID.randomUUID().toString();
        User user = new User("Test", "User", unique + "@example.com", unique, "password", null, List.of());
        em.persist(user);
        return user;
    }
    Card card(Account account, CardStatus status, boolean deleted) {
        User owner = account.getOwner();
        CardHolder holder = new CardHolder(UUID.randomUUID().toString(), account, owner,
                "Test", "Street", "City", "State", "US", "12345", CardHolderStatus.active);
        em.persist(holder);
        Wallet wallet = new Wallet("Test", UUID.randomUUID().toString(), account);
        em.persist(wallet);
        Card card = new Card(UUID.randomUUID().toString(), holder, wallet, owner, "4242", status, "visa", "virtual", "usd", 2030L);
        card.setIsDeleted(deleted);
        em.persist(card);
        return card;
    }

    @Test void countsAccountResourcesAndUsesSubscriptionOverrides() {
        tx.executeWithoutResult(s -> {
            Account account = em.find(Account.class, accountId);
            card(account, CardStatus.active, false);
            card(account, CardStatus.inactive, false);
            card(account, CardStatus.canceled, false);
            card(account, CardStatus.active, true);
            Account other = new Account("other@example.com", UUID.randomUUID().toString(), UUID.randomUUID().toString(), account.getOwner());
            em.persist(other); card(other, CardStatus.active, false);
        });
        assertThat(checker.execute(accountId, AccountResource.CARDS, 3).allowed()).isTrue();
        assertThat(checker.execute(accountId, AccountResource.CARDS, 4).allowed()).isFalse();
        assertThat(checker.execute(accountId, AccountResource.MEMBERS, 1).used()).isEqualTo(1);
        tx.executeWithoutResult(s -> em.persist(new SubscriptionEntitlement(em.find(Subscription.class, subscriptionId),
                em.find(Entitlement.class, cardEntitlementId), "1")));
        assertThat(checker.execute(accountId, AccountResource.CARDS, 0).allowed()).isFalse();
        tx.executeWithoutResult(s -> em.createQuery("update SubscriptionEntitlement e set e.value = '10' where e.subscription.id = :id")
                .setParameter("id", subscriptionId).executeUpdate());
        assertThat(checker.execute(accountId, AccountResource.CARDS, 8).allowed()).isTrue();
    }

    @Test void eligibleStatusesAndPeriodBoundaries() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 29, 12, 0);
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            tx.executeWithoutResult(s -> {
                Subscription sub = em.find(Subscription.class, subscriptionId);
                sub.setStatus(status); sub.setCurrentPeriodStart(now); sub.setCurrentPeriodEnd(now.plusDays(1));
                sub.setCancelAtPeriodEnd(true);
            });
            var eligible = subscriptions.findEligibleForResourceLimits(accountId, now,
                    List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING));
            assertThat(eligible.size()).isEqualTo(status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIALING ? 1 : 0);
        }
        tx.executeWithoutResult(s -> em.find(Subscription.class, subscriptionId).setStatus(SubscriptionStatus.ACTIVE));
        assertThat(subscriptions.findEligibleForResourceLimits(accountId, now.minusNanos(1), List.of(SubscriptionStatus.ACTIVE))).isEmpty();
        assertThat(subscriptions.findEligibleForResourceLimits(accountId, now.plusDays(1), List.of(SubscriptionStatus.ACTIVE))).isEmpty();
    }

    @Test void concurrentMemberCreationCannotClaimTheSameSlot() throws Exception {
        List<UUID> users = tx.execute(s -> List.of(user().getId(), user().getId()));
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (UUID id : users) results.add(executor.submit(() -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("start timed out");
                try { addMember.execute(new CreateMemberDto(accountId, id, Role.ADMIN)); return true; }
                catch (ResourceLimitException ex) { assertThat(ex.getCode()).isEqualTo("LIMIT_EXCEEDED"); return false; }
            }));
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            int successes = 0;
            for (Future<Boolean> result : results) if (result.get(15, TimeUnit.SECONDS)) successes++;
            assertThat(successes).isEqualTo(1);
            assertThat(checker.execute(accountId, AccountResource.MEMBERS, 0).used()).isEqualTo(2);
        } finally { executor.shutdownNow(); }
    }

    @Test void failedReplacementKeepsCancellationAndReservationAndCanRetry() {
        UUID originalId = tx.execute(s -> card(em.find(Account.class, accountId), CardStatus.active, false).getId());
        var dto = new ReplaceCardDto(CardReplacementReason.lost);
        when(stripeCreate.execute(any(), any(), any(), any(), any(), eq("replace-card-" + originalId)))
                .thenThrow(new RuntimeException("Stripe unavailable"));
        assertThatThrownBy(() -> replaceCard.execute(originalId, dto)).hasMessage("Stripe unavailable");
        tx.executeWithoutResult(s -> {
            Card original = em.find(Card.class, originalId);
            assertThat(original.getStatus()).isEqualTo(CardStatus.canceled);
            assertThat(original.isReplacementPending()).isTrue();
        });
        assertThat(checker.execute(accountId, AccountResource.CARDS, 0).used()).isEqualTo(1);
        assertThatThrownBy(() -> deleteCard.execute(originalId)).isInstanceOf(ResourceLimitException.class);
        var stripeCard = new com.stripe.model.issuing.Card();
        stripeCard.setId("replacement_" + originalId); stripeCard.setLast4("1234"); stripeCard.setStatus("active");
        stripeCard.setBrand("visa"); stripeCard.setType("virtual"); stripeCard.setCurrency("usd"); stripeCard.setExpYear(2030L);
        when(stripeCreate.execute(any(), any(), any(), any(), any(), eq("replace-card-" + originalId))).thenReturn(stripeCard);
        Card replacement = replaceCard.execute(originalId, dto);
        Card retry = replaceCard.execute(originalId, dto);
        assertThat(retry.getId()).isEqualTo(replacement.getId());
        assertThat(checker.execute(accountId, AccountResource.CARDS, 0).used()).isEqualTo(1);
        verify(stripeEdit, times(1)).execute(any(), any(), eq(CardStatus.canceled));
        verify(stripeCreate, times(2)).execute(any(), any(), any(), any(), any(), eq("replace-card-" + originalId));
    }

    @Test void concurrentCardCreationCannotClaimTheSameSlot() throws Exception {
        CreateCardDto dto = tx.execute(s -> {
            Account account = em.find(Account.class, accountId);
            Card original = card(account, CardStatus.active, false);
            em.persist(new SubscriptionEntitlement(em.find(Subscription.class, subscriptionId),
                    em.find(Entitlement.class, cardEntitlementId), "2"));
            CreateCardDto request = new CreateCardDto();
            request.setAccountId(accountId); request.setUserId(userId);
            request.setCardHolderId(original.getCardHolder().getId()); request.setWalletId(original.getTreasuryAccount().getId());
            return request;
        });
        when(stripeCreate.execute(any(), any(), any())).thenAnswer(invocation -> {
            var card = new com.stripe.model.issuing.Card();
            card.setId(UUID.randomUUID().toString()); card.setLast4("1234"); card.setStatus("active");
            card.setBrand("visa"); card.setType("virtual"); card.setCurrency("usd"); card.setExpYear(2030L);
            return card;
        });
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        Callable<Boolean> add = () -> {
            ready.countDown();
            if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("start timed out");
            try { createCard.execute(dto); return true; }
            catch (ResourceLimitException ex) { assertThat(ex.getCode()).isEqualTo("LIMIT_EXCEEDED"); return false; }
        };
        try {
            Future<Boolean> first = executor.submit(add), second = executor.submit(add);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue(); start.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
            assertThat(checker.execute(accountId, AccountResource.CARDS, 0).used()).isEqualTo(2);
            verify(stripeCreate, times(1)).execute(any(), any(), any());
        } finally { executor.shutdownNow(); }
    }

    @Test void canceledAndDeletedCardsCannotBeReactivated() {
        UUID canceled = tx.execute(s -> card(em.find(Account.class, accountId), CardStatus.canceled, false).getId());
        UUID deleted = tx.execute(s -> card(em.find(Account.class, accountId), CardStatus.inactive, true).getId());
        assertThatThrownBy(() -> editCard.execute(canceled, new EditCardDto(CardStatus.active, null)))
                .isInstanceOf(ResourceLimitException.class);
        assertThatThrownBy(() -> editCard.execute(deleted, new EditCardDto(CardStatus.active, null)))
                .isInstanceOf(ResourceLimitException.class);
        verifyNoInteractions(stripeEdit);
    }

    @Test void downgradeAllowsReplacementButDeniesGrowth() {
        UUID originalId = tx.execute(s -> {
            em.persist(new SubscriptionEntitlement(em.find(Subscription.class, subscriptionId),
                    em.find(Entitlement.class, cardEntitlementId), "0"));
            return card(em.find(Account.class, accountId), CardStatus.active, false).getId();
        });
        assertThat(checker.execute(accountId, AccountResource.CARDS, 1).allowed()).isFalse();
        var stripeCard = new com.stripe.model.issuing.Card();
        stripeCard.setId("replacement_" + originalId); stripeCard.setLast4("1234"); stripeCard.setStatus("active");
        stripeCard.setBrand("visa"); stripeCard.setType("virtual"); stripeCard.setCurrency("usd"); stripeCard.setExpYear(2030L);
        when(stripeCreate.execute(any(), any(), any(), any(), any(), any())).thenReturn(stripeCard);
        assertThat(replaceCard.execute(originalId, new ReplaceCardDto(CardReplacementReason.damaged))).isNotNull();
        assertThat(checker.execute(accountId, AccountResource.CARDS, 0).used()).isEqualTo(1);
    }
}
