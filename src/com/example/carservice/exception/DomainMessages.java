package com.example.carservice.exception;

public class DomainMessages {
    public static String invalidVin = "VIN must not be null or empty.";
    public static String invalidPhoneNumber = "Phone number must not be null or empty.";
    public static String invalidGiftName = "Gift name must not be null or empty.";
    public static String invalidGiftThreshold = "Gift threshold must be greater than zero.";
    public static String mechanicAlreadyAssigned = "Mechanic is already assigned to an order.";
    public static String costsRequired = "Parts and labor costs are required.";
    public static String costsMustBePositive = "Parts and labor costs must be positive.";
    public static String workAlreadyCompleted = "Service work is already completed.";
    public static String invalidDiagnoseStatus = "Only created orders can be diagnosed.";
    public static String invalidApproveStatus = "Only diagnosed orders can be approved.";
    public static String emptyOrder = "An order cannot be approved without service work.";
    public static String invalidProgressStatus = "Only approved orders can start progress.";
    public static String mechanicRequired = "A mechanic must be assigned before work starts.";
    public static String mechanicNotCompatible = "The mechanic is not compatible with the order work.";
    public static String invalidCompleteStatus = "Only in-progress orders can be completed.";
    public static String unfinishedWork = "An order cannot be completed with unfinished work.";
    public static String closedOrder = "A completed or cancelled order cannot be modified.";
    public static String invalidCancelStatus = "Cannot cancel order in status: ";
    public static String workRequired = "Service work is required.";

    private DomainMessages() {
    }
}
