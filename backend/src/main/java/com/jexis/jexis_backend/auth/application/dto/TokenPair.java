package com.jexis.jexis_backend.auth.application.dto;

/**
 * TokenPair
 *
 * Data Transfer Object used for representing a pair of access and refresh
 * tokens.
 *
 * Author: Leo
 */
public record TokenPair(String accessToken, String refreshToken) {
    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}