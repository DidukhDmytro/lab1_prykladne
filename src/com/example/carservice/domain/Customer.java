package com.example.carservice.domain;

import com.example.carservice.exception.InvalidArgumentException;

public class Customer {
    private static String INVALID_PHONE_NUMBER_MESSAGE = "Phone number must not be null or empty.";

    private String id;
    private String fullName;
    private String phoneNumber;

    public Customer(String id, String fullName, String phoneNumber) {
        validatePhoneNumber(phoneNumber);
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void updatePhoneNumber(String newPhoneNumber) {
        validatePhoneNumber(newPhoneNumber);
        phoneNumber = newPhoneNumber;
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new InvalidArgumentException(INVALID_PHONE_NUMBER_MESSAGE);
        }
    }
}
