package com.jexis.jexis_backend.cardholder.application.dto;

public record BillingAddressDto(
        String city,
        String country,
        String line1,
        String line2,
        String postalCode,
        String state) {

}
