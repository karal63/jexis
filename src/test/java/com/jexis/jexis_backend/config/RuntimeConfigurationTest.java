package com.jexis.jexis_backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;
import static org.assertj.core.api.Assertions.*;

class RuntimeConfigurationTest {
    private void initialize(String... profiles) {
        try (var context = new GenericApplicationContext()) {
            var env = new MockEnvironment();
            env.setActiveProfiles(profiles);
            context.setEnvironment(env);
            new RuntimeConfiguration().initialize(context);
        }
    }

    @Test void rejectsMissingAndConflictingProfiles() {
        assertThatThrownBy(() -> initialize()).hasMessageContaining("exactly one");
        assertThatThrownBy(() -> initialize("dev", "prod")).hasMessageContaining("exactly one");
        assertThatThrownBy(() -> initialize("prod", "test")).hasMessageContaining("exactly one");
    }

    @Test void rejectsMissingCredentialsBeforeConnecting() {
        assertThatThrownBy(() -> initialize("prod")).hasMessage("Missing configuration: DB_URL");
    }

    @Test void allowsIsolatedTestProfile() {
        assertThatCode(() -> initialize("test")).doesNotThrowAnyException();
    }
}
