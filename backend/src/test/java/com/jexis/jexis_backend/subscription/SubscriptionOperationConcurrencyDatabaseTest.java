package com.jexis.jexis_backend.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import com.jexis.jexis_backend.account.application.useCases.LockAccountUseCase;
import com.jexis.jexis_backend.account.domain.entities.Account;
import com.jexis.jexis_backend.account.infrastructure.AccountRepository;
import com.jexis.jexis_backend.plan.domain.entities.Plan;
import com.jexis.jexis_backend.plan.domain.enums.PlanStatus;
import com.jexis.jexis_backend.subscription.application.useCases.SubscriptionOperationCommands;
import com.jexis.jexis_backend.subscription.domain.enums.SubscriptionOperationType;
import com.jexis.jexis_backend.subscription.infrastructure.PendingSubscriptionOperationRepository;
import com.jexis.jexis_backend.subscription.infrastructure.SubscriptionRepository;
import com.jexis.jexis_backend.user.domain.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SubscriptionOperationConcurrencyDatabaseTest.Config.class)
class SubscriptionOperationConcurrencyDatabaseTest {
    @TestConfiguration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {
            AccountRepository.class,
            SubscriptionRepository.class,
            PendingSubscriptionOperationRepository.class
    })
    @Import({LockAccountUseCase.class, SubscriptionOperationCommands.class})
    static class Config {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(
                    "jdbc:h2:mem:subscription_operations;MODE=PostgreSQL;"
                            + "NON_KEYWORDS=KEY,VALUE,INTERVAL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
                    "sa", "");
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.jexis.jexis_backend");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setJpaPropertyMap(Map.of(
                    "hibernate.hbm2ddl.auto", "create-drop",
                    "hibernate.physical_naming_strategy",
                    "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                    "hibernate.implicit_naming_strategy",
                    "org.springframework.boot.hibernate.SpringImplicitNamingStrategy"));
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
            return new JpaTransactionManager(entityManagerFactory);
        }
    }

    @PersistenceContext EntityManager entityManager;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired SubscriptionOperationCommands commands;
    @Autowired PendingSubscriptionOperationRepository operations;
    private UUID accountId;
    private UUID userId;
    private UUID planId;

    @BeforeEach
    void setUp() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            String unique = UUID.randomUUID().toString();
            User user = new User("Test", "User", unique + "@example.com", unique,
                    "password", null, List.of());
            entityManager.persist(user);
            Account account = new Account("account@example.com", "acct_" + unique,
                    "link_" + unique, user);
            entityManager.persist(account);
            Plan plan = new Plan("prod_" + unique, "Paid", "paid_" + unique,
                    null, true, PlanStatus.PUBLISHED);
            entityManager.persist(plan);
            accountId = account.getId();
            userId = user.getId();
            planId = plan.getId();
        });
    }

    @Test
    void simultaneousReservationsReturnTheSameOperation() throws Exception {
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<UUID> reserve = () -> {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                return commands.prepare(accountId, userId, planId,
                        SubscriptionOperationType.CHECKOUT, null).operation().getId();
            };
            var first = executor.submit(reserve);
            var second = executor.submit(reserve);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            assertThat(first.get(15, TimeUnit.SECONDS)).isEqualTo(second.get(15, TimeUnit.SECONDS));
            assertThat(operations.count()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
