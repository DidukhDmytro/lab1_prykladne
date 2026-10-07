package com.example.carservice.domain;

public class Car {
    private String vin;
    private String make;
    private String model;
    private int year;
    private Customer owner;

    public Car(String vin, String make, String model, int year, Customer owner) {
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
}
