package com.jexis.jexis_backend.auth.application.dto;

import com.jexis.jexis_backend.user.application.dto.UserResponseDto;

/**
 * LoginResult
 *
 * Data Transfer Object used for login results. It encapsulates the
 * necessary data required to return the result of a login request, such as
 * the authenticated user and their tokens.
 *
 * Author: Leo
 */
public record LoginResult(UserResponseDto user, TokenPair tokens) {
}
