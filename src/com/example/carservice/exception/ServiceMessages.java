package com.example.carservice.exception;

public class ServiceMessages {
    public static String entityRequired = "Registered entities must not be null.";
    public static String duplicateEntity = "An entity with identifier '%s' is already registered.";
    public static String duplicateVin = "A car with VIN '%s' is already registered.";
    public static String unregisteredCarOrder = "Cannot create order for unregistered car with VIN: %s";
    public static String mechanicRequired = "A mechanic is required for order assignment.";
    public static String orderRequired = "An order is required for mechanic assignment.";
    public static String orderRequiredForOperation = "An order is required for this operation.";
    public static String mechanicAlreadyAssigned = "The mechanic is already assigned to an order.";
    public static String mechanicNotCompatible = "The mechanic is not compatible with all work in the order.";

    private ServiceMessages() {
    }
}
