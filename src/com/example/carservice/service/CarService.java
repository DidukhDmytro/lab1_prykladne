package com.example.carservice.service;

import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.domain.MechanicSpecialization;
import com.example.carservice.domain.ServiceOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.exception.EntityAlreadyExistsException;
import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.MechanicAlreadyAssignedException;
import com.example.carservice.exception.MechanicNotCompatibleException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CarService {
    private static String ENTITY_REQUIRED_MESSAGE = "Registered entities must not be null.";
    private static String DUPLICATE_ENTITY_MESSAGE = "An entity with identifier '%s' is already registered.";
    private static String DUPLICATE_VIN_MESSAGE = "A car with VIN '%s' is already registered.";
    private static String MECHANIC_REQUIRED_MESSAGE = "A mechanic is required for order assignment.";
    private static String ORDER_REQUIRED_MESSAGE = "An order is required for mechanic assignment.";
    private static String MECHANIC_ALREADY_ASSIGNED_MESSAGE = "The mechanic is already assigned to an order.";
    private static String MECHANIC_NOT_COMPATIBLE_MESSAGE =
            "The mechanic is not compatible with all work in the order.";

    private Map<String, Customer> customers = new LinkedHashMap<>();
    private Map<String, Car> carsByVin = new LinkedHashMap<>();
    private Map<String, Mechanic> mechanics = new LinkedHashMap<>();
    private Map<String, ServiceOrder> orders = new LinkedHashMap<>();

    public void registerCustomer(Customer customer) {
        if (customer == null) {
            throw new InvalidArgumentException(ENTITY_REQUIRED_MESSAGE);
        }
        registerEntity(customers, customer.getId(), customer);
    }

    public void registerCar(Car car) {
        if (car == null) {
            throw new InvalidArgumentException(ENTITY_REQUIRED_MESSAGE);
        }
        if (carsByVin.containsKey(car.getVin())) {
            throw new EntityAlreadyExistsException(String.format(DUPLICATE_VIN_MESSAGE, car.getVin()));
        }
        carsByVin.put(car.getVin(), car);
    }

    public void registerMechanic(Mechanic mechanic) {
        if (mechanic == null) {
            throw new InvalidArgumentException(ENTITY_REQUIRED_MESSAGE);
        }
        registerEntity(mechanics, mechanic.getId(), mechanic);
    }

    public void registerOrder(ServiceOrder order) {
        if (order == null) {
            throw new InvalidArgumentException(ENTITY_REQUIRED_MESSAGE);
        }
        registerEntity(orders, order.getId(), order);
    }

    public void assignMechanicToOrder(ServiceOrder order, Mechanic mechanic) {
        if (order == null) {
            throw new InvalidArgumentException(ORDER_REQUIRED_MESSAGE);
        }
        if (mechanic == null) {
            throw new InvalidArgumentException(MECHANIC_REQUIRED_MESSAGE);
        }
        if (mechanic.isBusy()) {
            throw new MechanicAlreadyAssignedException(MECHANIC_ALREADY_ASSIGNED_MESSAGE);
        }
        if (!isMechanicCompatibleWithAllWork(order, mechanic)) {
            throw new MechanicNotCompatibleException(MECHANIC_NOT_COMPATIBLE_MESSAGE);
        }
        order.assignMechanic(mechanic);
    }

    private <T> void registerEntity(Map<String, T> registry, String id, T entity) {
        if (registry.containsKey(id)) {
            throw new EntityAlreadyExistsException(String.format(DUPLICATE_ENTITY_MESSAGE, id));
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
