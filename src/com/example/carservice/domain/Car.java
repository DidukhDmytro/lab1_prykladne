package com.example.carservice.domain;

import com.example.carservice.exception.InvalidArgumentException;

public class Car {
    private static String INVALID_VIN_MESSAGE = "VIN must not be null or empty.";

    private String vin;
    private String make;
    private String model;
    private int year;
    private Customer owner;

    public Car(String vin, String make, String model, int year, Customer owner) {
        validateVin(vin);
        this.vin = vin;
        this.make = make;
        this.model = model;
        this.year = year;
        this.owner = owner;
    }

    public String getVin() {
        return vin;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public int getYear() {
        return year;
    }

    public Customer getOwner() {
        return owner;
    }

    private void validateVin(String vin) {
        // VIN is required to identify a car.
        if (vin == null || vin.isEmpty()) {
            throw new InvalidArgumentException(INVALID_VIN_MESSAGE);
        }
    }
}
