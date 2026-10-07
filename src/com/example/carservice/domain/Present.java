package com.example.carservice.domain;

import com.example.carservice.exception.InvalidArgumentException;
import java.math.BigDecimal;
import java.util.Optional;

public class Present {
    private static BigDecimal WASHER_FLUID_MINIMUM = new BigDecimal("200");
    private static BigDecimal INTERIOR_CLEANING_MINIMUM = new BigDecimal("500");
    private static BigDecimal DISCOUNT_VOUCHER_MINIMUM = new BigDecimal("1000");
    private static String WASHER_FLUID_NAME = "Washer fluid";
    private static String INTERIOR_CLEANING_NAME = "Interior cleaning";
    private static String DISCOUNT_VOUCHER_NAME = "Discount voucher for your next visit";
    private static String INVALID_PARTS_TOTAL_MESSAGE = "Parts total must not be null or negative.";

    private BigDecimal partsTotal;
    private Gift gift;

    public Present(BigDecimal partsTotal) {
        if (partsTotal == null || partsTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidArgumentException(INVALID_PARTS_TOTAL_MESSAGE);
        }
        this.partsTotal = partsTotal;
        this.gift = determineGift(partsTotal);
    }

    public BigDecimal getPartsTotal() {
        return partsTotal;
    }

    public Optional<Gift> getGift() {
        return Optional.ofNullable(gift);
    }

    private Gift determineGift(BigDecimal partsTotal) {
        Gift discountVoucher = new Gift(DISCOUNT_VOUCHER_NAME, DISCOUNT_VOUCHER_MINIMUM);
        if (discountVoucher.isAvailableFor(partsTotal)) {
            return discountVoucher;
        }
        Gift interiorCleaning = new Gift(INTERIOR_CLEANING_NAME, INTERIOR_CLEANING_MINIMUM);
        if (interiorCleaning.isAvailableFor(partsTotal)) {
            return interiorCleaning;
        }
        Gift washerFluid = new Gift(WASHER_FLUID_NAME, WASHER_FLUID_MINIMUM);
        if (washerFluid.isAvailableFor(partsTotal)) {
            return washerFluid;
        }
        return null;
    }
}
