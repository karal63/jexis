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
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
        validateEntities();
        UUID userId = insertUserAndAccount();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM accounts WHERE owner_id = ?", Integer.class, userId)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM flyway_schema_history WHERE success", Integer.class)).isEqualTo(1);
    }

    @Test
    void existingDatabaseRequiresExplicitBaselineAndRetainsRelationships() {
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V1__initial_schema.sql")).execute(dataSource);
        UUID userId = insertUserAndAccount();
        assertThatThrownBy(() -> flyway.migrate()).hasMessageContaining("non-empty schema");
        validateEntities();
        flyway.baseline();
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM accounts WHERE owner_id = ?", Integer.class, userId)).isEqualTo(1);
    }

    @Test
    void incompatibleSchemaFailsHibernateValidation() {
        flyway.migrate();
        jdbc.execute("ALTER TABLE users DROP COLUMN first_name");
        assertThatThrownBy(this::validateEntities).hasStackTraceContaining("first_name");
    }
}
