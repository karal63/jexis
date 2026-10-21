package com.jexis.jexis_backend.account.application.dto;

public record AccountResourceLimitDto(
        boolean allowed, long used, Long limit, long remaining, long additionalQuantity, String reason
) {}
