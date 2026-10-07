package com.example.carservice.domain;

import com.example.carservice.exception.InvalidArgumentException;
import java.math.BigDecimal;

public class Gift {
    private static String INVALID_NAME_MESSAGE = "Gift name must not be null or empty.";
    private static String INVALID_THRESHOLD_MESSAGE = "Gift threshold must be greater than zero.";
    private static String INVALID_PARTS_TOTAL_MESSAGE = "Parts total must not be null.";

    private String name;
    private BigDecimal minimumPartsTotal;

    public Gift(String name, BigDecimal minimumPartsTotal) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidArgumentException(INVALID_NAME_MESSAGE);
        }
        if (minimumPartsTotal == null || minimumPartsTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException(INVALID_THRESHOLD_MESSAGE);
        }
        this.name = name;
        this.minimumPartsTotal = minimumPartsTotal;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getMinimumPartsTotal() {
        return minimumPartsTotal;
    }

    public boolean isAvailableFor(BigDecimal partsTotal) {
        if (partsTotal == null) {
            throw new InvalidArgumentException(INVALID_PARTS_TOTAL_MESSAGE);
        }
        return partsTotal.compareTo(minimumPartsTotal) >= 0;
    }
}
