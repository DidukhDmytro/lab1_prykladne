package com.example.carservice.validator;

import com.example.carservice.domain.Mechanic;
import com.example.carservice.domain.ServiceOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.ServiceMessages;
import java.math.BigDecimal;

public class ServiceValidator {
    public static void validateEntityRequired(Object entity) {
        if (entity == null) {
            throw new InvalidArgumentException(ServiceMessages.entityRequired);
        }
    }

    public static void validateMechanicRequired(Mechanic mechanic) {
        if (mechanic == null) {
            throw new InvalidArgumentException(ServiceMessages.mechanicRequired);
        }
    }

    public static void validateAssignmentOrderRequired(ServiceOrder order) {
        if (order == null) {
            throw new InvalidArgumentException(ServiceMessages.orderRequired);
        }
    }

    public static void validateOrderRequired(ServiceOrder order) {
        if (order == null) {
            throw new InvalidArgumentException(ServiceMessages.orderRequiredForOperation);
        }
    }

    public static void validateWorkRequired(ServiceWork work) {
        if (work == null) {
            throw new InvalidArgumentException(DomainMessages.workRequired);
        }
    }

    public static void validateVin(String vin) {
        if (vin == null || vin.isEmpty()) {
            throw new InvalidArgumentException(DomainMessages.invalidVin);
        }
    }

    public static void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new InvalidArgumentException(DomainMessages.invalidPhoneNumber);
        }
    }

    public static void validateCosts(BigDecimal partsCost, BigDecimal laborCost) {
        if (partsCost == null || laborCost == null) {
            throw new InvalidArgumentException(DomainMessages.costsRequired);
        }
        if (partsCost.compareTo(BigDecimal.ZERO) <= 0 || laborCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException(DomainMessages.costsMustBePositive);
        }
    }

    public static void validateGift(String name, BigDecimal minimumPartsTotal) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidArgumentException(DomainMessages.invalidGiftName);
        }
        if (minimumPartsTotal == null || minimumPartsTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException(DomainMessages.invalidGiftThreshold);
        }
    }

    public static void validateOrderWorks(ServiceOrder order) {
        for (ServiceWork work : order.getWorks()) {
            validateWorkRequired(work);
            validateCosts(work.getPartsCost(), work.getLaborCost());
        }
    }

    private ServiceValidator() {
    }
}
