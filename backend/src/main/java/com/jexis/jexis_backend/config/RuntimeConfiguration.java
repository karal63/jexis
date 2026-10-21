package com.jexis.jexis_backend.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/** Validate the selected environment before opening database or broker connections. */
public class RuntimeConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var env = context.getEnvironment();
        var profiles = Arrays.asList(env.getActiveProfiles());
        if (profiles.equals(List.of("test"))) return;
        if (profiles.contains("dev") == profiles.contains("prod") || profiles.contains("test")) {
            throw new IllegalStateException("Select exactly one runtime profile: dev or prod (use scripts/dev or scripts/prod).");
        }
        for (String key : List.of("DB_URL", "DB_USERNAME", "DB_PASSWORD", "RABBITMQ_HOST",
                "RABBITMQ_PORT", "RABBITMQ_USERNAME", "RABBITMQ_PASSWORD", "APP_ORIGIN", "PUBLIC_API_URL",
                "JWT_ACCESS_SECRET", "JWT_REFRESH_SECRET", "STRIPE_API_KEY", "STRIPE_ACCOUNT_WEBHOOK",
                "STRIPE_WALLET_WEBHOOK", "STRIPE_CARD_WEBHOOK", "STRIPE_AUTHORIZATION_WEBHOOK",
                "STRIPE_DISPUTE_WEBHOOK", "STRIPE_SUBSCRIPTION_WEBHOOK", "STRIPE_INVOICE_WEBHOOK",
                "MAIL_HOST", "MAIL_PORT", "MAIL_USERNAME", "MAIL_PASSWORD")) {
            String value = env.getProperty(key);
            if (value == null || value.isBlank() || value.startsWith("CHANGE_ME")) {
                throw new IllegalStateException("Missing configuration: " + key);
            }
        }
    }
}
