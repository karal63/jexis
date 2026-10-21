package com.jexis.jexis_backend.user.application.dto;

public record PaymentMethodResponseDto(
    String id,
    String brand,
    String last4,
    String expMonth,
    String expYear
) {
}
