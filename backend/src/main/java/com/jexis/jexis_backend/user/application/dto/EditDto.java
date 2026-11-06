package com.jexis.jexis_backend.user.application.dto;


/** Partial profile update: omitted values remain unchanged; an explicitly supplied phone may clear it. */
public class EditDto {
    private String firstName;

    private String lastName;

    private String phoneNumber;
    private boolean phoneNumberProvided;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.phoneNumberProvided = true;
    }

    public boolean isPhoneNumberProvided() {
        return phoneNumberProvided;
    }

}
