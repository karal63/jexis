package com.jexis.jexis_backend.user.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * EditDto
 * <p>
 * Data Transfer Object used for user editing requests. It encapsulates the
 * necessary data required to edit an existing user, such as the user name,
 * email,
 * and password.
 * <p>
 * Author: Leo
 */
public class EditDto {
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Pattern(regexp = "^[\\d+\\-()\\s]*$", message = "Invalid phone format")
    private String phoneNumber;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
