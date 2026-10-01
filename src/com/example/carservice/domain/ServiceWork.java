package com.example.carservice.domain;

import com.example.carservice.exception.InvalidArgumentException;
import java.math.BigDecimal;

public class ServiceWork {
    private static String COSTS_REQUIRED_MESSAGE = "Parts and labor costs are required.";
    private static String COSTS_MUST_BE_POSITIVE_MESSAGE = "Parts and labor costs must be positive.";
    private static String WORK_ALREADY_COMPLETED_MESSAGE = "Service work is already completed.";

    private String description;
    private BigDecimal partsCost;
    private BigDecimal laborCost;
    private MechanicSpecialization requiredSpecialization;
    private WorkStatus status;

    public ServiceWork(
            String description,
            BigDecimal partsCost,
            BigDecimal laborCost,
            MechanicSpecialization requiredSpecialization) {
        validateCosts(partsCost, laborCost);
        this.description = description;
        this.partsCost = partsCost;
        this.laborCost = laborCost;
        this.requiredSpecialization = requiredSpecialization;
        this.status = WorkStatus.PENDING;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPartsCost() {
        return partsCost;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public MechanicSpecialization getRequiredSpecialization() {
        return requiredSpecialization;
    }

    public WorkStatus getStatus() {
        return status;
    }

    public void complete() {
        if (status == WorkStatus.COMPLETED) {
            throw new InvalidArgumentException(WORK_ALREADY_COMPLETED_MESSAGE);
        }
        status = WorkStatus.COMPLETED;
    }

    private void validateCosts(BigDecimal partsCost, BigDecimal laborCost) {
        if (partsCost == null || laborCost == null) {
            throw new InvalidArgumentException(COSTS_REQUIRED_MESSAGE);
        }
        // Positive costs ensure every work item has a valid charge.
        if (partsCost.compareTo(BigDecimal.ZERO) <= 0 || laborCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException(COSTS_MUST_BE_POSITIVE_MESSAGE);
        }
    }
}
