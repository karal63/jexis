package com.jexis.jexis_backend.auth.application.dto;

import java.util.List;
import java.util.UUID;

import com.jexis.jexis_backend.user.domain.enums.UserRole;

/**
 * AuthUser
 *
 * Data Transfer Object used for authentication requests. It encapsulates the
 * necessary data required for user authentication, such as the user ID, name,
 * email, and activation status.
 *
 * Author: Leo
 */
public record AuthUser(
        UUID id,
        String name,
        String email,
        Boolean isActivated,
        List<UserRole> roles) {
}