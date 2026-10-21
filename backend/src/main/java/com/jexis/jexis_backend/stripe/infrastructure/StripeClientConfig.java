package com.jexis.jexis_backend.stripe.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeClientConfig {
    @Bean
    public com.stripe.StripeClient stripeClient(@Value("${stripe.api.key}") String apiKey) {
        return new com.stripe.StripeClient(apiKey);
    }
}
