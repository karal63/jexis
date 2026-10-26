package com.jexis.jexis_backend.config;

import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import static org.assertj.core.api.Assertions.*;

/** Every test owns a random schema; no existing tables or schemas are modified. */
@Tag("postgres")
class MigrationIntegrationTest {
    private DriverManagerDataSource dataSource;
    private JdbcTemplate jdbc;
    private String schema;
    private Flyway flyway;

    @BeforeEach
    void setup() {
        schema = "migration_test_" + UUID.randomUUID().toString().replace("-", "");
        dataSource = new DriverManagerDataSource(System.getenv("TEST_DATABASE_URL"),
                System.getenv().getOrDefault("TEST_DATABASE_USERNAME", "postgres"),
                System.getenv().getOrDefault("TEST_DATABASE_PASSWORD", ""));
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE SCHEMA " + schema);
        var properties = new java.util.Properties();
        properties.setProperty("currentSchema", schema);
        dataSource.setConnectionProperties(properties);
        flyway = Flyway.configure().dataSource(dataSource).schemas(schema)
                .baselineOnMigrate(false).cleanDisabled(true).load();
    }

    @AfterEach
    void cleanup() {
        if (jdbc != null && schema != null) jdbc.execute("DROP SCHEMA " + schema + " CASCADE");
    }

    private void validateEntities() {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.jexis.jexis_backend");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "validate",
                "hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                "hibernate.implicit_naming_strategy", "org.springframework.boot.hibernate.SpringImplicitNamingStrategy"));
        try { factory.afterPropertiesSet(); }
        finally { factory.destroy(); }
    }

    private UUID insertUserAndAccount() {
        UUID userId = UUID.randomUUID();
        jdbc.update("INSERT INTO users (id, first_name, last_name, email, phone_number, password, is_activated, is_deleted, created_at) VALUES (?, 'Test', 'User', 'test@example.com', '123', 'hashed', false, false, now())", userId);
        jdbc.update("INSERT INTO accounts (id, owner_id, account_link, connect_account_id, is_deleted, created_at) VALUES (?, ?, 'link', 'acct_test', false, now())", UUID.randomUUID(), userId);
        return userId;
    }

    @Test
    void freshDatabaseMigratesValidatesAndPreservesDataOnRestart() {
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(6);
        validateEntities();
        UUID userId = insertUserAndAccount();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM accounts WHERE owner_id = ?", Integer.class, userId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE success", Integer.class)).isEqualTo(6);
    }

    @Test
    void existingDatabaseRequiresExplicitBaselineAndRetainsRelationships() {
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V1__initial_schema.sql")).execute(dataSource);
        UUID userId = insertUserAndAccount();
        assertThatThrownBy(() -> flyway.migrate()).hasMessageContaining("non-empty schema");
        flyway.baseline();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(5);
        validateEntities();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM accounts WHERE owner_id = ?", Integer.class, userId)).isEqualTo(1);
    }

    @Test
    void freePlanSupportsLocalSubscriptionsAndPreventsDuplicateActiveProvisioning() {
        flyway.migrate();
        UUID userId = insertUserAndAccount();
        UUID accountId = jdbc.queryForObject("SELECT id FROM accounts WHERE owner_id = ?", UUID.class, userId);
        UUID planId = jdbc.queryForObject("SELECT id FROM plans WHERE code = 'free'", UUID.class);
        UUID priceId = jdbc.queryForObject("SELECT default_price_id FROM plans WHERE id = ?", UUID.class, planId);
        assertThat(jdbc.queryForObject("SELECT unit_amount FROM prices WHERE id = ?", Long.class, priceId)).isZero();
        String insert = """
                INSERT INTO subscriptions (id, user_id, account_id, plan_id, price_id, status,
                    current_period_start, created_at, cancel_at_period_end)
                VALUES (?, ?, ?, ?, ?, 'ACTIVE', now(), now(), false)
                """;
        jdbc.update(insert, UUID.randomUUID(), userId, accountId, planId, priceId);
        assertThatThrownBy(() -> jdbc.update(insert, UUID.randomUUID(), userId, accountId, planId, priceId))
                .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM subscriptions WHERE stripe_subscription_id IS NULL AND current_period_end IS NULL", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void walletMigrationPreservesExistingAmountsAndSupportsLargeBalances() {
        Flyway.configure().dataSource(dataSource).schemas(schema).target("2").load().migrate();
        UUID userId = insertUserAndAccount();
        UUID accountId = jdbc.queryForObject("SELECT id FROM accounts WHERE owner_id = ?", UUID.class, userId);
        UUID walletId = UUID.randomUUID();
        jdbc.update("INSERT INTO wallets (id, account_id, name, stripe_financial_account_id, available_balance, is_deleted, created_at) VALUES (?, ?, 'Expenses', 'fa_test', 12345, false, now())", walletId, accountId);
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(4);
        assertThat(jdbc.queryForObject("SELECT available_balance FROM wallets WHERE id = ?", Long.class, walletId)).isEqualTo(12345L);
        assertThat(jdbc.queryForObject("SELECT currency FROM wallets WHERE id = ?", String.class, walletId)).isEqualTo("USD");
        jdbc.update("UPDATE wallets SET available_balance = ? WHERE id = ?", 3_000_000_000L, walletId);
        assertThat(jdbc.queryForObject("SELECT available_balance FROM wallets WHERE id = ?", Long.class, walletId)).isEqualTo(3_000_000_000L);
        assertThatThrownBy(() -> jdbc.update("UPDATE wallets SET currency = 'EUR' WHERE id = ?", walletId))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE wallets SET currency = NULL WHERE id = ?", walletId))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        validateEntities();
    }

    @Test
    void merchantMigrationLeavesHistoricalValuesUnknownAndStoresOriginalAmounts() {
        Flyway.configure().dataSource(dataSource).schemas(schema).target("3").load().migrate();
        UUID userId = insertUserAndAccount();
        UUID accountId = jdbc.queryForObject("SELECT id FROM accounts WHERE owner_id = ?", UUID.class, userId);
        UUID walletId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        jdbc.update("INSERT INTO wallets (id, account_id, name, stripe_financial_account_id, available_balance, currency, is_deleted, created_at) VALUES (?, ?, 'Expenses', 'fa_test', 12345, 'USD', false, now())", walletId, accountId);
        jdbc.update("INSERT INTO transactions (id, wallet_id, type, amount, currency, direction, status) VALUES (?, ?, 'CARD_TRANSACTION', -1000, 'usd', 'DEBIT', 'COMPLETED')", transactionId, walletId);
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT merchant_amount FROM transactions WHERE id = ?", Long.class, transactionId)).isNull();
        assertThat(jdbc.queryForObject("SELECT merchant_currency FROM transactions WHERE id = ?", String.class, transactionId)).isNull();
        jdbc.update("UPDATE transactions SET merchant_amount = ?, merchant_currency = 'jpy' WHERE id = ?", -3_000_000_000L, transactionId);
        assertThat(jdbc.queryForObject("SELECT merchant_amount FROM transactions WHERE id = ?", Long.class, transactionId)).isEqualTo(-3_000_000_000L);
        assertThat(jdbc.queryForObject("SELECT merchant_currency FROM transactions WHERE id = ?", String.class, transactionId)).isEqualTo("jpy");
        assertThat(jdbc.queryForObject("SELECT amount FROM transactions WHERE id = ?", Long.class, transactionId)).isEqualTo(-1000L);
        assertThat(jdbc.queryForObject("SELECT currency FROM transactions WHERE id = ?", String.class, transactionId)).isEqualTo("usd");
        validateEntities();
    }

    @Test
    void sessionMigrationPreservesUsersAndPersistsRevocation() {
        Flyway.configure().dataSource(dataSource).schemas(schema).target("4").load().migrate();
        UUID userId = insertUserAndAccount();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
        UUID sessionId = UUID.randomUUID();
        jdbc.update("INSERT INTO auth_sessions (id, user_id, refresh_token_hash, expires_at, created_at) VALUES (?, ?, ?, now() + interval '30 days', now())",
                sessionId, userId, "a".repeat(64));
        jdbc.update("UPDATE auth_sessions SET revoked_at = now() WHERE id = ?", sessionId);
        assertThat(jdbc.queryForObject("SELECT revoked_at IS NOT NULL FROM auth_sessions WHERE id = ?", Boolean.class, sessionId)).isTrue();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM users WHERE id = ?", Integer.class, userId)).isEqualTo(1);
        validateEntities();
    }

    @Test
    void incompatibleSchemaFailsHibernateValidation() {
        flyway.migrate();
        jdbc.execute("ALTER TABLE users DROP COLUMN first_name");
        assertThatThrownBy(this::validateEntities).hasStackTraceContaining("first_name");
    }
}
