package com.jexis.jexis_backend.auth.application.dto;

import com.jexis.jexis_backend.common.validation.ValidPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * SignupDto
 *
 * Data Transfer Object used for signup requests. It encapsulates the
 * necessary data required to create a new user account, such as the user's
 * name, email, and password.
 *
 * Author: Leo
 */
public class SignupDto {
    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    private String phoneNumber;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    private String email;

    @ValidPassword
    private String password;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
