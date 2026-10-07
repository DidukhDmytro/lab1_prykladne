package com.example.carservice.service;

import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.Gift;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.domain.MechanicSpecialization;
import com.example.carservice.domain.OrderPriority;
import com.example.carservice.domain.Present;
import com.example.carservice.domain.ServiceOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.EntityAlreadyExistsException;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.MechanicAlreadyAssignedException;
import com.example.carservice.exception.MechanicNotCompatibleException;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class CarService {
    private static String entityRequiredMessage = "Registered entities must not be null.";
    private static String duplicateEntityMessage = "An entity with identifier '%s' is already registered.";
    private static String duplicateVinMessage = "A car with VIN '%s' is already registered.";
    private static String unregisteredCarOrderMessage =
            "Cannot create order for unregistered car with VIN: %s";
    private static String mechanicRequiredMessage = "A mechanic is required for order assignment.";
    private static String orderRequiredMessage = "An order is required for mechanic assignment.";
    private static String orderRequiredForOperationMessage = "An order is required for this operation.";
    private static String mechanicAlreadyAssignedMessage = "The mechanic is already assigned to an order.";
    private static String mechanicNotCompatibleMessage =
            "The mechanic is not compatible with all work in the order.";
    private static String washerFluidName = "Washer fluid";
    private static String interiorCleaningName = "Interior cleaning";
    private static String discountVoucherName = "Discount voucher for your next visit";
    private static BigDecimal washerFluidMinimum = new BigDecimal("200");
    private static BigDecimal interiorCleaningMinimum = new BigDecimal("500");
    private static BigDecimal discountVoucherMinimum = new BigDecimal("1000");
    private static BigDecimal urgentLaborSurchargeFactor = new BigDecimal("1.20");

    private Map<String, Customer> customers = new LinkedHashMap<>();
    private Map<String, Car> carsByVin = new LinkedHashMap<>();
    private Map<String, Mechanic> mechanics = new LinkedHashMap<>();
    private Map<String, ServiceOrder> orders = new LinkedHashMap<>();

    public Customer createCustomer(String id, String fullName, String phoneNumber) {
        validatePhoneNumber(phoneNumber);
        return new Customer(id, fullName, phoneNumber);
    }

    public Car createCar(String vin, String make, String model, int year, Customer owner) {
        validateVin(vin);
        return new Car(vin, make, model, year, owner);
    }

    public ServiceWork createServiceWork(
            String description,
            BigDecimal partsCost,
            BigDecimal laborCost,
            MechanicSpecialization requiredSpecialization) {
        validateCosts(partsCost, laborCost);
        return new ServiceWork(description, partsCost, laborCost, requiredSpecialization);
    }

    public void updateCustomerPhoneNumber(Customer customer, String phoneNumber) {
        if (customer == null) {
            throw new InvalidArgumentException(entityRequiredMessage);
        }
        validatePhoneNumber(phoneNumber);
        customer.updatePhoneNumber(phoneNumber);
    }

    public void addWork(ServiceOrder order, ServiceWork work) {
        if (order == null) {
            throw new InvalidArgumentException(orderRequiredForOperationMessage);
        }
        if (work == null) {
            throw new InvalidArgumentException(DomainMessages.workRequired);
        }
        validateCosts(work.getPartsCost(), work.getLaborCost());
        order.addWork(work);
    }

    public BigDecimal calculatePartsTotal(ServiceOrder order) {
        requireOrder(order);
        BigDecimal partsTotal = BigDecimal.ZERO;
        for (ServiceWork work : order.getWorks()) {
            partsTotal = partsTotal.add(work.getPartsCost());
        }
        return partsTotal;
    }

    public BigDecimal calculateTotalCost(ServiceOrder order) {
        requireOrder(order);
        BigDecimal partsCost = calculatePartsTotal(order);
        BigDecimal laborCost = BigDecimal.ZERO;
        for (ServiceWork work : order.getWorks()) {
            laborCost = laborCost.add(work.getLaborCost());
        }
        if (order.getPriority() == OrderPriority.URGENT) {
            laborCost = laborCost.multiply(urgentLaborSurchargeFactor);
        }
        return partsCost.add(laborCost);
    }

    public Present createPresent(ServiceOrder order) {
        BigDecimal partsTotal = calculatePartsTotal(order);
        Gift gift = createAvailableGift(partsTotal);
        return new Present(partsTotal, gift);
    }

    public void registerCustomer(Customer customer) {
        if (customer == null) {
            throw new InvalidArgumentException(entityRequiredMessage);
        }
        validatePhoneNumber(customer.getPhoneNumber());
        registerEntity(customers, customer.getId(), customer);
    }

    public void registerCar(Car car) {
        if (car == null) {
            throw new InvalidArgumentException(entityRequiredMessage);
        }
        validateVin(car.getVin());
        if (carsByVin.containsKey(car.getVin())) {
            throw new EntityAlreadyExistsException(String.format(duplicateVinMessage, car.getVin()));
        }
        carsByVin.put(car.getVin(), car);
    }

    public void registerMechanic(Mechanic mechanic) {
        if (mechanic == null) {
            throw new InvalidArgumentException(entityRequiredMessage);
        }
        registerEntity(mechanics, mechanic.getId(), mechanic);
    }

    public void registerOrder(ServiceOrder order) {
        if (order == null) {
            throw new InvalidArgumentException(entityRequiredMessage);
        }
        Car car = order.getCar();
        if (car == null || !carsByVin.containsKey(car.getVin())) {
            String vin = car == null ? "null" : car.getVin();
            throw new EntityNotFoundException(String.format(unregisteredCarOrderMessage, vin));
        }
        validateOrderWorks(order);
        registerEntity(orders, order.getId(), order);
    }

    public void assignMechanicToOrder(ServiceOrder order, Mechanic mechanic) {
        if (order == null) {
            throw new InvalidArgumentException(orderRequiredMessage);
        }
        if (mechanic == null) {
            throw new InvalidArgumentException(mechanicRequiredMessage);
        }
        validateOrderWorks(order);
        if (mechanic.isBusy()) {
            throw new MechanicAlreadyAssignedException(mechanicAlreadyAssignedMessage);
        }
        if (!isMechanicCompatibleWithAllWork(order, mechanic)) {
            throw new MechanicNotCompatibleException(mechanicNotCompatibleMessage);
        }
        order.assignMechanic(mechanic);
    }

    private Gift createAvailableGift(BigDecimal partsTotal) {
        if (isAvailableFor(partsTotal, discountVoucherMinimum)) {
            return createGift(discountVoucherName, discountVoucherMinimum);
        }
        if (isAvailableFor(partsTotal, interiorCleaningMinimum)) {
            return createGift(interiorCleaningName, interiorCleaningMinimum);
        }
        if (isAvailableFor(partsTotal, washerFluidMinimum)) {
            return createGift(washerFluidName, washerFluidMinimum);
        }
        return null;
    }

    private Gift createGift(String name, BigDecimal minimumPartsTotal) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidArgumentException(DomainMessages.invalidGiftName);
        }
        if (minimumPartsTotal == null || minimumPartsTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException(DomainMessages.invalidGiftThreshold);
        }
        return new Gift(name, minimumPartsTotal);
    }

    private boolean isAvailableFor(BigDecimal partsTotal, BigDecimal minimumPartsTotal) {
        return partsTotal.compareTo(minimumPartsTotal) >= 0;
    }

    private void validateVin(String vin) {
        if (vin == null || vin.isEmpty()) {
            throw new InvalidArgumentException(DomainMessages.invalidVin);
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new InvalidArgumentException(DomainMessages.invalidPhoneNumber);
        }
    }

    private void validateCosts(BigDecimal partsCost, BigDecimal laborCost) {
        if (partsCost == null || laborCost == null) {
            throw new InvalidArgumentException(DomainMessages.costsRequired);
        }
        if (partsCost.compareTo(BigDecimal.ZERO) <= 0 || laborCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException(DomainMessages.costsMustBePositive);
        }
    }

    private void requireOrder(ServiceOrder order) {
        if (order == null) {
            throw new InvalidArgumentException(orderRequiredForOperationMessage);
        }
    }

    private void validateOrderWorks(ServiceOrder order) {
        for (ServiceWork work : order.getWorks()) {
            if (work == null) {
                throw new InvalidArgumentException(DomainMessages.workRequired);
            }
            validateCosts(work.getPartsCost(), work.getLaborCost());
        }
    }

    private <T> void registerEntity(Map<String, T> registry, String id, T entity) {
        if (registry.containsKey(id)) {
            throw new EntityAlreadyExistsException(String.format(duplicateEntityMessage, id));
        }
        registry.put(id, entity);
    }

    // General mechanics can perform any work; other mechanics must match every work specialization.
    private boolean isMechanicCompatibleWithAllWork(ServiceOrder order, Mechanic mechanic) {
        return order.getWorks().stream().allMatch(work -> isCompatible(work, mechanic));
    }

    private boolean isCompatible(ServiceWork work, Mechanic mechanic) {
        MechanicSpecialization requiredSpecialization = work.getRequiredSpecialization();
        return requiredSpecialization == MechanicSpecialization.GENERAL
                || mechanic.getSpecialization() == MechanicSpecialization.GENERAL
                || requiredSpecialization == mechanic.getSpecialization();
    }
}
