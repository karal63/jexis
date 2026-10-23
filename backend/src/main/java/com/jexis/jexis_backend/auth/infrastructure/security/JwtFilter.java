package com.jexis.jexis_backend.auth.infrastructure.security;

import com.jexis.jexis_backend.auth.application.security.AuthSessionService;
import com.jexis.jexis_backend.auth.domain.exception.InvalidRtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final AuthSessionService sessions;

    public JwtFilter(JwtUtil jwtUtil, AuthSessionService sessions) {
        this.jwtUtil = jwtUtil;
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (!"access_token".equals(cookie.getName())) continue;
                try {
                    sessions.authenticate(jwtUtil.extractClaimsAt(cookie.getValue())).ifPresent(user -> {
                        var auth = new UsernamePasswordAuthenticationToken(user, null, List.of());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                } catch (JwtException | IllegalArgumentException | InvalidRtException ex) {
                    // Invalid, expired and legacy tokens leave this request unauthenticated.
                    SecurityContextHolder.clearContext();
                }
                break;
            }
        }
        filterChain.doFilter(request, response);
    }
}
