package com.jexis.jexis_backend.auth.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jexis.jexis_backend.auth.application.dto.TokenPair;
import com.jexis.jexis_backend.user.domain.enums.UserRole;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    private final Key ACCESS_TOKEN;
    private final Key REFRESH_TOKEN;
    private final long atExpirationMs = TimeUnit.MINUTES.toMillis(15);
    private final long rtExpirationMs = TimeUnit.DAYS.toMillis(30);

    public JwtUtil(@Value("${jwt.secret.access}") String accessSecret,
            @Value("${jwt.secret.refresh}") String refreshSecret) {
        this.ACCESS_TOKEN = Keys.hmacShaKeyFor(accessSecret.getBytes());
        this.REFRESH_TOKEN = Keys.hmacShaKeyFor(refreshSecret.getBytes());
    }

    public TokenPair generateTokens(UUID id, String name, String email, Boolean isActivated, List<UserRole> roles) {
        String accessToken = Jwts.builder()
                .claim("id", id.toString())
                .claim("name", name)
                .claim("email", email)
                .claim("isActivated", isActivated)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + atExpirationMs))
                .signWith(ACCESS_TOKEN)
                .compact();

        String refreshToken = Jwts.builder()
                .claim("id", id.toString())
                .claim("name", name)
                .claim("email", email)
                .claim("isActivated", isActivated)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + rtExpirationMs))
                .signWith(REFRESH_TOKEN)
                .compact();

        return new TokenPair(accessToken, refreshToken);
    }

    public boolean validateAccessToken(String token) {
        try {
            extractClaimsAt(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            extractClaimsRt(token);
            return true;
        } catch (JwtException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Claims extractClaimsAt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(ACCESS_TOKEN)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Claims extractClaimsRt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(REFRESH_TOKEN)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}