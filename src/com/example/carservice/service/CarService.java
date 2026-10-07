package com.example.carservice.service;

import com.example.carservice.config.AppConstants;
import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.Gift;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.domain.MechanicSpecialization;
import com.example.carservice.domain.OrderPriority;
import com.example.carservice.domain.Present;
import com.example.carservice.domain.ServiceOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.exception.EntityAlreadyExistsException;
import com.example.carservice.exception.EntityNotFoundException;
import com.example.carservice.exception.MechanicAlreadyAssignedException;
import com.example.carservice.exception.MechanicNotCompatibleException;
import com.example.carservice.exception.ServiceMessages;
import com.example.carservice.validator.ServiceValidator;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class CarService {
    private Map<String, Customer> customers = new LinkedHashMap<>();
    private Map<String, Car> carsByVin = new LinkedHashMap<>();
    private Map<String, Mechanic> mechanics = new LinkedHashMap<>();
    private Map<String, ServiceOrder> orders = new LinkedHashMap<>();

    public Customer createCustomer(String id, String fullName, String phoneNumber) {
        ServiceValidator.validatePhoneNumber(phoneNumber);
        return new Customer(id, fullName, phoneNumber);
    }

    public Car createCar(String vin, String make, String model, int year, Customer owner) {
        ServiceValidator.validateVin(vin);
        return new Car(vin, make, model, year, owner);
    }

    public ServiceWork createServiceWork(
            String description,
            BigDecimal partsCost,
            BigDecimal laborCost,
            MechanicSpecialization requiredSpecialization) {
        ServiceValidator.validateCosts(partsCost, laborCost);
        return new ServiceWork(description, partsCost, laborCost, requiredSpecialization);
    }

    public void updateCustomerPhoneNumber(Customer customer, String phoneNumber) {
        ServiceValidator.validateEntityRequired(customer);
        ServiceValidator.validatePhoneNumber(phoneNumber);
        customer.updatePhoneNumber(phoneNumber);
    }

    public void addWork(ServiceOrder order, ServiceWork work) {
        ServiceValidator.validateOrderRequired(order);
        ServiceValidator.validateWorkRequired(work);
        ServiceValidator.validateCosts(work.getPartsCost(), work.getLaborCost());
        order.addWork(work);
    }

    public BigDecimal calculatePartsTotal(ServiceOrder order) {
        ServiceValidator.validateOrderRequired(order);
        BigDecimal partsTotal = BigDecimal.ZERO;
        for (ServiceWork work : order.getWorks()) {
            partsTotal = partsTotal.add(work.getPartsCost());
        }
        return partsTotal;
    }

    public BigDecimal calculateTotalCost(ServiceOrder order) {
        ServiceValidator.validateOrderRequired(order);
        BigDecimal partsCost = calculatePartsTotal(order);
        BigDecimal laborCost = BigDecimal.ZERO;
        for (ServiceWork work : order.getWorks()) {
            laborCost = laborCost.add(work.getLaborCost());
        }
        if (order.getPriority() == OrderPriority.URGENT) {
            laborCost = laborCost.multiply(AppConstants.urgentLaborSurchargeFactor);
        }
        return partsCost.add(laborCost);
    }

    public Present createPresent(ServiceOrder order) {
        BigDecimal partsTotal = calculatePartsTotal(order);
        Gift gift = createAvailableGift(partsTotal);
        return new Present(partsTotal, gift);
    }

    public void registerCustomer(Customer customer) {
        ServiceValidator.validateEntityRequired(customer);
        ServiceValidator.validatePhoneNumber(customer.getPhoneNumber());
        registerEntity(customers, customer.getId(), customer);
    }

    public void registerCar(Car car) {
        ServiceValidator.validateEntityRequired(car);
        ServiceValidator.validateVin(car.getVin());
        if (carsByVin.containsKey(car.getVin())) {
            throw new EntityAlreadyExistsException(String.format(ServiceMessages.duplicateVin, car.getVin()));
        }
        carsByVin.put(car.getVin(), car);
    }

    public void registerMechanic(Mechanic mechanic) {
        ServiceValidator.validateEntityRequired(mechanic);
        registerEntity(mechanics, mechanic.getId(), mechanic);
    }

    public void registerOrder(ServiceOrder order) {
        ServiceValidator.validateEntityRequired(order);
        Car car = order.getCar();
        if (car == null || !carsByVin.containsKey(car.getVin())) {
            String vin = car == null ? null : car.getVin();
            throw new EntityNotFoundException(String.format(ServiceMessages.unregisteredCarOrder, vin));
        }
        ServiceValidator.validateOrderWorks(order);
        registerEntity(orders, order.getId(), order);
    }

    public void assignMechanicToOrder(ServiceOrder order, Mechanic mechanic) {
        ServiceValidator.validateAssignmentOrderRequired(order);
        ServiceValidator.validateMechanicRequired(mechanic);
        ServiceValidator.validateOrderWorks(order);
        if (mechanic.isBusy()) {
            throw new MechanicAlreadyAssignedException(ServiceMessages.mechanicAlreadyAssigned);
        }
        if (!isMechanicCompatibleWithAllWork(order, mechanic)) {
            throw new MechanicNotCompatibleException(ServiceMessages.mechanicNotCompatible);
        }
        order.assignMechanic(mechanic);
    }

    private Gift createAvailableGift(BigDecimal partsTotal) {
        if (isAvailableFor(partsTotal, AppConstants.discountVoucherMinimum)) {
            return createGift(AppConstants.discountVoucherName, AppConstants.discountVoucherMinimum);
        }
        if (isAvailableFor(partsTotal, AppConstants.interiorCleaningMinimum)) {
            return createGift(AppConstants.interiorCleaningName, AppConstants.interiorCleaningMinimum);
        }
        if (isAvailableFor(partsTotal, AppConstants.washerFluidMinimum)) {
            return createGift(AppConstants.washerFluidName, AppConstants.washerFluidMinimum);
        }
        return null;
    }

    private Gift createGift(String name, BigDecimal minimumPartsTotal) {
        ServiceValidator.validateGift(name, minimumPartsTotal);
        return new Gift(name, minimumPartsTotal);
    }

    private boolean isAvailableFor(BigDecimal partsTotal, BigDecimal minimumPartsTotal) {
        return partsTotal.compareTo(minimumPartsTotal) >= 0;
    }

    private <T> void registerEntity(Map<String, T> registry, String id, T entity) {
        if (registry.containsKey(id)) {
            throw new EntityAlreadyExistsException(String.format(ServiceMessages.duplicateEntity, id));
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
