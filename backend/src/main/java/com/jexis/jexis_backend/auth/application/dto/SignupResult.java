package com.jexis.jexis_backend.auth.application.dto;

import com.jexis.jexis_backend.user.application.dto.UserResponseDto;

/**
 * SignupResult
 *
 * Data Transfer Object used for signup results. It encapsulates the
 * necessary data required to return the result of a signup request, such as
 * the newly created user and their tokens.
 *
 * Author: Leo
 */
public record SignupResult(UserResponseDto user, TokenPair tokens) {
}
