package com.jexis.jexis_backend.auth.infrastructure.config;

import com.jexis.jexis_backend.auth.application.security.AuthSessionService;
import org.springframework.security.config.http.SessionCreationPolicy;
import com.jexis.jexis_backend.common.ratelimit.AuthRateLimitFilter;
import com.jexis.jexis_backend.common.ratelimit.RateLimitService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.jexis.jexis_backend.auth.infrastructure.security.JwtFilter;
import com.jexis.jexis_backend.auth.infrastructure.security.JwtUtil;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final RateLimitService rateLimits;
    private final JwtUtil jwtUtil;
    private final AuthSessionService sessions;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    public SecurityConfig(JwtUtil jwtUtil, JwtAuthEntryPoint jwtAuthEntryPoint, RateLimitService rateLimits, AuthSessionService sessions) {
        this.rateLimits = rateLimits;
        this.sessions = sessions;
        this.jwtUtil = jwtUtil;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        JwtFilter jwtFilter = new JwtFilter(jwtUtil, sessions);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/auth/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/webjars/**", "/webhooks/**", "/user/activate", "/users/reset-password/confirm")
                        .permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthEntryPoint))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new AuthRateLimitFilter(rateLimits), JwtFilter.class);

        return http.build();
    }

}