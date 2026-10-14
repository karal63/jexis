package com.jexis.jexis_backend.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

/** An offline reference schema for reviewing migrations; never connects to a database. */
class SchemaExportTest {
    @Test
    void exportsPostgresReferenceSchema() throws Exception {
        Path output = Path.of("build/reference-schema.sql").toAbsolutePath();
        Files.deleteIfExists(output);
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setPackagesToScan("com.jexis.jexis_backend");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
                "hibernate.boot.allow_jdbc_metadata_access", "false",
                "hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                "hibernate.implicit_naming_strategy", "org.springframework.boot.hibernate.SpringImplicitNamingStrategy",
                "jakarta.persistence.schema-generation.database.action", "none",
                "jakarta.persistence.schema-generation.scripts.action", "create",
                "jakarta.persistence.schema-generation.scripts.create-target", output.toString(),
                "hibernate.format_sql", "true"));
        try { factory.afterPropertiesSet(); }
        finally { factory.destroy(); }
    }
}
