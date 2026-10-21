package com.jexis.jexis_backend.common.ratelimit;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

// Registered only in Spring Security, before JWT processing (not as a servlet bean).
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService limits;

    public AuthRateLimitFilter(RateLimitService limits) { this.limits = limits; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String path = request.getServletPath();
        String policy = null;
        if ("POST".equals(request.getMethod())) {
            policy = switch (path) {
                case "/auth/login" -> "login-ip";
                case "/auth/signup" -> "signup-ip";
                case "/auth/forgot-password", "/users/change-password" -> "reset-ip";
                case "/users/reset-password/confirm" -> "confirm-ip";
                case "/auth/refresh" -> "refresh-ip";
                default -> null;
            };
        } else if ("GET".equals(request.getMethod()) && "/user/activate".equals(path)) {
            policy = "activate-ip";
        }
        if (policy != null) {
            try {
                // Forwarded headers must be sanitized by the trusted ingress proxy.
                limits.check(policy, request.getRemoteAddr());
            } catch (RateLimitException ex) {
                response.setStatus(ex.status());
                response.setContentType("application/json");
                response.setHeader("Retry-After", Long.toString(ex.retryAfter()));
                response.setHeader("Cache-Control", "no-store");
                response.getWriter().write("{\"status\":" + ex.status() + ",\"code\":\"" + ex.code()
                        + "\",\"message\":\"" + ex.getMessage() + "\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
