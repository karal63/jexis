package com.jexis.jexis_backend.member;

import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.card.domain.entities.Card;
import com.jexis.jexis_backend.card.domain.enums.CardStatus;
import com.jexis.jexis_backend.card.infrastructure.CardRepository;
import com.jexis.jexis_backend.card.application.useCases.GetMemberCardsUseCase;
import com.jexis.jexis_backend.cardholder.domain.entities.CardHolder;
import com.jexis.jexis_backend.cardholder.domain.enums.CardHolderStatus;
import com.jexis.jexis_backend.transaction.domain.entities.Transaction;
import com.jexis.jexis_backend.transaction.domain.enums.*;
import com.jexis.jexis_backend.transaction.infrastructure.TransactionRepository;
import com.jexis.jexis_backend.transaction.application.useCases.GetMemberTransactionsUseCase;
import com.jexis.jexis_backend.authorization.domain.entities.Authorization;
import com.jexis.jexis_backend.authorization.domain.enums.AuthorizationStatus;
import com.jexis.jexis_backend.authorization.infrastructure.AuthorizationRepository;
import com.jexis.jexis_backend.authorization.application.useCases.GetMemberAuthorizationsUseCase;
import com.jexis.jexis_backend.dispute.domain.entities.Dispute;
import com.jexis.jexis_backend.dispute.domain.enums.DisputeStatus;
import com.jexis.jexis_backend.dispute.application.dto.DisputeReason;
import com.jexis.jexis_backend.dispute.infrastructure.DisputeRepository;
import com.jexis.jexis_backend.dispute.application.useCases.GetMemberDisputesUseCase;
import com.jexis.jexis_backend.member.application.useCases.GetMemberUseCase;
import com.jexis.jexis_backend.member.infrastructure.MemberRepository;
import com.jexis.jexis_backend.member.domain.entities.Member;
import com.jexis.jexis_backend.member.domain.enums.Role;
import com.jexis.jexis_backend.user.domain.entities.User;
import com.jexis.jexis_backend.wallet.domain.entities.Wallet;
import jakarta.persistence.*;
import java.util.*;
import javax.sql.DataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MemberResourcesDatabaseTest.Config.class)
@Transactional
class MemberResourcesDatabaseTest {
    @Configuration @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {CardRepository.class, TransactionRepository.class,
            AuthorizationRepository.class, DisputeRepository.class, MemberRepository.class})
    @Import({GetMemberUseCase.class, GetMemberCardsUseCase.class, GetMemberTransactionsUseCase.class,
            GetMemberAuthorizationsUseCase.class, GetMemberDisputesUseCase.class})
    static class Config {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:member_resources;MODE=PostgreSQL;NON_KEYWORDS=KEY,VALUE,INTERVAL;DB_CLOSE_DELAY=-1", "sa", "");
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(ds); factory.setPackagesToScan("com.jexis.jexis_backend");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }
        @Bean PlatformTransactionManager transactionManager(EntityManagerFactory emf) { return new JpaTransactionManager(emf); }
    }
    @PersistenceContext EntityManager em;
    @Autowired GetMemberCardsUseCase cards;
    @Autowired GetMemberTransactionsUseCase transactions;
    @Autowired GetMemberAuthorizationsUseCase authorizations;
    @Autowired GetMemberDisputesUseCase disputes;
    @Autowired AuthorizationRepository authorizationRepository;
    Account account;
    Member member;
    Card first, second, deleted;

    @BeforeEach void seed() {
        User user = user(); account = account(user);
        member = new Member(account, user, Role.MEMBER); em.persist(member);
        first = records(account, user, false, "Alpha", 100L);
        second = records(account, user, false, "Beta", 200L);
        deleted = records(account, user, true, "Deleted", 300L);
        records(account, user(), false, "Other member", 400L);
        records(account(user), user, false, "Other company", 500L);
        // Bank transfers have no member/card owner and must not appear in member lists.
        Wallet bankWallet = new Wallet("Bank", unique(), account); em.persist(bankWallet);
        em.persist(new Transaction(bankWallet, null, unique(), TransactionType.OUTBOUND_TRANSFER, 600L, "usd",
                TransactionStatus.COMPLETED, TransactionDirection.DEBIT));
        em.flush();
    }
    User user() {
        User u = new User("Test", "Member", unique() + "@example.com", unique(), "password", null, List.of());
        em.persist(u); return u;
    }
    String unique() { return UUID.randomUUID().toString(); }
    Account account(User owner) {
        Account a = new Account(unique() + "@example.com", unique(), unique(), owner); em.persist(a); return a;
    }
    Card records(Account account, User user, boolean deleted, String merchant, long amount) {
        Wallet wallet = new Wallet(merchant, unique(), account); em.persist(wallet);
        CardHolder holder = new CardHolder(unique(), account, user, "Test", "Street", "City", "State", "US", "12345", CardHolderStatus.active);
        em.persist(holder);
        Card card = new Card(unique(), holder, wallet, user, "4242", CardStatus.active, "visa", "virtual", "usd", 2030L);
        card.setIsDeleted(deleted); em.persist(card);
        Authorization auth = new Authorization(unique(), wallet, card, true, amount, "usd", AuthorizationStatus.CLOSED);
        auth.setMerchantName(merchant); em.persist(auth);
        Transaction transaction = new Transaction(wallet, unique(), unique(), TransactionType.CARD_TRANSACTION,
                amount, "usd", TransactionStatus.COMPLETED, TransactionDirection.DEBIT);
        transaction.setCard(card); transaction.setAuthorization(auth); transaction.setMerchantName(merchant); em.persist(transaction);
        em.persist(new Dispute(unique(), transaction, wallet, amount, "usd", DisputeStatus.WON, DisputeReason.fraudulent));
        return card;
    }
    @Test void listsAreScopedToMemberAndCompanyAcrossAllWallets() {
        UUID a = account.getId(), m = member.getId();
        assertThat(cards.execute(a, m, 1, 20, null, null, null, null, null, null).getContent())
                .extracting(Card::getStripeCardId).containsExactlyInAnyOrder(first.getStripeCardId(), second.getStripeCardId());
        assertThat(transactions.execute(a, m, 1, 20, null, null, null, null, null, null).getContent())
                .extracting(Transaction::getAmount).containsExactlyInAnyOrder(100L, 200L, 300L);
        assertThat(authorizations.execute(a, m, 1, 20, null, null, null, null, null).getContent())
                .extracting(Authorization::getAmount).containsExactlyInAnyOrder(100L, 200L, 300L);
        assertThat(disputes.execute(a, m, 1, 20, null, null, null, null, null).getContent())
                .extracting(Dispute::getAmount).containsExactlyInAnyOrder(100L, 200L, 300L);
    }
    @Test void filtersSortingPaginationAndCountsRemainScoped() {
        UUID a = account.getId(), m = member.getId();
        var result = transactions.execute(a, m, 2, 1, null, TransactionType.CARD_TRANSACTION,
                TransactionStatus.COMPLETED, TransactionDirection.DEBIT, "amount", "asc");
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getContent()).extracting(Transaction::getAmount).containsExactly(200L);
        assertThat(authorizations.execute(a, m, 1, 20, " ALPHA ", true, AuthorizationStatus.CLOSED, "merchantname", "asc")
                .getContent()).extracting(Authorization::getMerchantName).containsExactly("Alpha");
        assertThat(authorizations.execute(a, m, 1, 20, null, false, null, null, null).getContent()).isEmpty();
        assertThat(cards.execute(a, m, 1, 20, null, CardStatus.active, "visa", "virtual", null, null).getTotalElements()).isEqualTo(2);
        assertThat(disputes.execute(a, m, 1, 20, null, DisputeReason.fraudulent, DisputeStatus.WON, null, null).getTotalElements()).isEqualTo(3);
        assertThat(disputes.execute(a, m, 1, 20, null, null, DisputeStatus.LOST, null, null).getContent()).isEmpty();
    }
    @Test void walletAuthorizationQueryCannotReturnOtherWallets() {
        var result = authorizationRepository.findWalletAuthorizationsWithFilters(first.getTreasuryAccount().getId(),
                PageRequest.of(0, 20), null, null, null);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(Authorization::getAmount).containsExactly(100L);
    }
}
