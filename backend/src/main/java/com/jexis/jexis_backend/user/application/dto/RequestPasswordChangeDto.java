package com.jexis.jexis_backend.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record RequestPasswordChangeDto(@NotNull @Email String email) {
}
