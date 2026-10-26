package com.jexis.jexis_backend.user.application.dto;

import com.jexis.jexis_backend.common.validation.ValidPassword;

/**
 * ChangePasswordDto
 *
 * Data Transfer Object used for password change requests.
 *
 * Author: Leo
 */
public class ChangePasswordDto {
    @ValidPassword
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
