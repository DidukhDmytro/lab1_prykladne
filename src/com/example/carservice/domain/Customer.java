package com.example.carservice.domain;

public class Customer {
    private String id;
    private String fullName;
    private String phoneNumber;

    public Customer(String id, String fullName, String phoneNumber) {
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
        phoneNumber = newPhoneNumber;
    }
}
