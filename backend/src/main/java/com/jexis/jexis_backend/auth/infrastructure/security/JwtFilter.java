package com.jexis.jexis_backend.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jexis.jexis_backend.auth.application.dto.AuthUser;
import com.jexis.jexis_backend.user.domain.enums.UserRole;

import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        Cookie accessToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    accessToken = cookie;
                    break;
                }
            }

            if (accessToken != null) {
                String tokenValue = accessToken.getValue();

                if (jwtUtil.validateAccessToken(tokenValue)) {
                    Claims claims = jwtUtil.extractClaimsAt(tokenValue);

                    UUID id = UUID.fromString(claims.get("id", String.class));
                    String name = claims.get("name", String.class);
                    String email = claims.get("email", String.class);
                    Boolean isActivated = claims.get("isActivated", Boolean.class);
                    List<?> rolesNames = claims.get("roles", List.class);
                    List<UserRole> roles = rolesNames.stream()
                            .map(role -> UserRole.valueOf(role.toString()))
                            .toList();

                    AuthUser user = new AuthUser(id, name, email, isActivated, roles);

                    var auth = new UsernamePasswordAuthenticationToken(
                            user, null, List.of());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

        }
        filterChain.doFilter(request, response);
    }
}