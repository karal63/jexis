package com.jexis.jexis_backend.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * LoginDto
 *
 * Data Transfer Object used for login requests. It encapsulates the
 * necessary data required for user login, such as the user's email and
 * password.
 *
 * Author: Leo
 */
public record LoginDto(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email,
        
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 6, max = 128, message = "Password must be between 6 and 128 characters")
        String password) {

}
