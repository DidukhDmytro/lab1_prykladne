package com.example.carservice.app;

import com.example.carservice.domain.Car;
import com.example.carservice.domain.Customer;
import com.example.carservice.domain.Mechanic;
import com.example.carservice.domain.MechanicSpecialization;
import com.example.carservice.domain.OrderPriority;
import com.example.carservice.domain.ServiceOrder;
import com.example.carservice.domain.ServiceWork;
import com.example.carservice.exception.DomainException;
import com.example.carservice.exception.InvalidStateTransitionException;
import com.example.carservice.service.CarService;
import java.math.BigDecimal;

public class CarServiceDemo {
    private static String CUSTOMER_ID = "customer-001";
    private static String CUSTOMER_NAME = "Alex Morgan";
    private static String CUSTOMER_PHONE = "+1-555-0100";
    private static String CAR_VIN = "DEMO-VIN-001";
    private static String CAR_MAKE = "Example Motors";
    private static String CAR_MODEL = "Roadster";
    private static int CAR_YEAR = 2024;
    private static String GENERAL_MECHANIC_ID = "mechanic-general";
    private static String ENGINE_MECHANIC_ID = "mechanic-engine";
    private static String BRAKES_MECHANIC_ID = "mechanic-brakes";
    private static String GENERAL_MECHANIC_NAME = "Taylor General";
    private static String ENGINE_MECHANIC_NAME = "Jordan Engine";
    private static String BRAKES_MECHANIC_NAME = "Casey Brakes";
    private static boolean MECHANIC_INITIAL_BUSY = false;
    private static String POSITIVE_ORDER_ID = "order-positive";
    private static String ENGINE_WORK_DESCRIPTION = "Engine service";
    private static String BRAKES_WORK_DESCRIPTION = "Brake service";
    private static String ENGINE_PARTS_COST_VALUE = "200.00";
    private static String ENGINE_LABOR_COST_VALUE = "85.00";
    private static String BRAKES_PARTS_COST_VALUE = "60.00";
    private static String BRAKES_LABOR_COST_VALUE = "45.00";
    private static BigDecimal ENGINE_PARTS_COST = new BigDecimal(ENGINE_PARTS_COST_VALUE);
    private static BigDecimal ENGINE_LABOR_COST = new BigDecimal(ENGINE_LABOR_COST_VALUE);
    private static BigDecimal BRAKES_PARTS_COST = new BigDecimal(BRAKES_PARTS_COST_VALUE);
    private static BigDecimal BRAKES_LABOR_COST = new BigDecimal(BRAKES_LABOR_COST_VALUE);
    private static String EMPTY_ORDER_ID = "order-empty";
    private static String INCOMPATIBLE_ORDER_ID = "order-incompatible";
    private static String BUSY_ORDER_ID = "order-busy";
    private static String OTHER_ACTIVE_ORDER_ID = "order-other-active";
    private static String NORMAL_PRIORITY_ORDER_ID = "order-normal-priority";
    private static String URGENT_PRIORITY_ORDER_ID = "order-urgent-priority";
    private static String INVALID_TRANSITION_ORDER_ID = "order-invalid-transition";
    private static String INTERIOR_CLEANING_GIFT_ORDER_ID = "order-interior-cleaning-gift";
    private static String DISCOUNT_VOUCHER_GIFT_ORDER_ID = "order-discount-voucher-gift";
    private static String GIFT_WORK_DESCRIPTION = "Parts order";
    private static String INTERIOR_CLEANING_PARTS_TOTAL_VALUE = "550.00";
    private static String DISCOUNT_VOUCHER_PARTS_TOTAL_VALUE = "1111.00";
    private static String GIFT_WORK_LABOR_COST_VALUE = "1.00";
    private static String DUPLICATE_VIN_TEST = "Duplicate VIN registration";
    private static String EMPTY_ORDER_TEST = "Approving an empty order";
    private static String INCOMPATIBLE_MECHANIC_TEST = "Assigning an incompatible mechanic";
    private static String BUSY_MECHANIC_TEST = "Assigning a busy mechanic";
    private static String COMPLETED_ORDER_TEST = "Modifying a completed order";
    private static String POSITIVE_FLOW_MESSAGE = "Positive order flow completed.";
    private static String TOTAL_COST_FORMAT = "Total order cost: %.2f";
    private static String PRIORITY_TOTAL_COST_FORMAT = "%s order total cost: %.2f";
    private static String NORMAL_PRIORITY_LABEL = "Normal";
    private static String URGENT_PRIORITY_LABEL = "Urgent";
    private static String EXPECTED_ERROR_FORMAT = "%s: expected error - %s";
    private static String INVALID_TRANSITION_ERROR_FORMAT =
            "Invalid status transition check: expected error -> %s";
    private static String GIFT_SCENARIO_FORMAT = "Parts total: %.2f; gift: %s";
    private static String NO_GIFT_LABEL = "No gift";

    private CarServiceDemo() {
    }

    public static void main(String[] args) {
        CarService carService = new CarService();
        Customer customer = new Customer(CUSTOMER_ID, CUSTOMER_NAME, CUSTOMER_PHONE);
        Car car = new Car(CAR_VIN, CAR_MAKE, CAR_MODEL, CAR_YEAR, customer);
        Mechanic generalMechanic = new Mechanic(
                GENERAL_MECHANIC_ID,
                GENERAL_MECHANIC_NAME,
                MechanicSpecialization.GENERAL,
                MECHANIC_INITIAL_BUSY);
        Mechanic engineMechanic = new Mechanic(
                ENGINE_MECHANIC_ID,
                ENGINE_MECHANIC_NAME,
                MechanicSpecialization.ENGINE,
                MECHANIC_INITIAL_BUSY);
        Mechanic brakesMechanic = new Mechanic(
                BRAKES_MECHANIC_ID,
                BRAKES_MECHANIC_NAME,
                MechanicSpecialization.BRAKES,
                MECHANIC_INITIAL_BUSY);

        carService.registerCustomer(customer);
        carService.registerCar(car);
        carService.registerMechanic(generalMechanic);
        carService.registerMechanic(engineMechanic);
        carService.registerMechanic(brakesMechanic);

        ServiceOrder positiveOrder = createApprovedOrder(
                carService,
                POSITIVE_ORDER_ID,
                car,
                createEngineWork(),
                createBrakesWork());
        carService.assignMechanicToOrder(positiveOrder, generalMechanic);
        positiveOrder.startProgress();
        completeWorks(positiveOrder);
        positiveOrder.complete();
        System.out.println(POSITIVE_FLOW_MESSAGE);
        System.out.println(String.format(TOTAL_COST_FORMAT, positiveOrder.calculateTotalCost()));
        positiveOrder.getPresent().getGift().ifPresent(gift ->
                System.out.println(String.format(GIFT_SCENARIO_FORMAT,
                        positiveOrder.calculatePartsTotal(), gift.getName())));
        demonstrateGiftTiers(car, carService);
        demonstratePriorityCostCalculation(car, carService);

        demonstrateDuplicateVinRejection(carService, customer);
        demonstrateEmptyOrderApprovalRejection(carService, car);
        demonstrateIncompatibleMechanicRejection(carService, car, brakesMechanic);
        demonstrateBusyMechanicRejection(carService, car, engineMechanic);
        demonstrateCompletedOrderModificationRejection(positiveOrder);

        ServiceOrder invalidTransitionOrder = new ServiceOrder(INVALID_TRANSITION_ORDER_ID, car);
        try {
            invalidTransitionOrder.complete();
        } catch (InvalidStateTransitionException exception) {
            System.out.println(String.format(
                    INVALID_TRANSITION_ERROR_FORMAT,
                    exception.getMessage()));
        }
    }

    private static void demonstrateDuplicateVinRejection(CarService carService, Customer customer) {
        // This verifies that registered VINs cannot be reused.
        try {
            carService.registerCar(new Car(CAR_VIN, CAR_MAKE, CAR_MODEL, CAR_YEAR, customer));
        } catch (DomainException exception) {
            printExpectedError(DUPLICATE_VIN_TEST, exception);
        }
    }

    private static void demonstrateEmptyOrderApprovalRejection(CarService carService, Car car) {
        // This verifies that approval requires at least one work item.
        ServiceOrder order = new ServiceOrder(EMPTY_ORDER_ID, car);
        carService.registerOrder(order);
        order.diagnose();
        try {
            order.approve();
        } catch (DomainException exception) {
            printExpectedError(EMPTY_ORDER_TEST, exception);
        }
    }

    private static void demonstrateIncompatibleMechanicRejection(
            CarService carService,
            Car car,
            Mechanic mechanic) {
        // This verifies that every work specialization must match the mechanic.
        ServiceOrder order = createApprovedOrder(
                carService,
                INCOMPATIBLE_ORDER_ID,
                car,
                createEngineWork());
        try {
            carService.assignMechanicToOrder(order, mechanic);
        } catch (DomainException exception) {
            printExpectedError(INCOMPATIBLE_MECHANIC_TEST, exception);
        }
    }

    private static void demonstrateBusyMechanicRejection(
            CarService carService,
            Car car,
            Mechanic mechanic) {
        // This verifies that a mechanic assigned to active work cannot be reused.
        ServiceOrder activeOrder = createApprovedOrder(
                carService,
                BUSY_ORDER_ID,
                car,
                createEngineWork());
        carService.assignMechanicToOrder(activeOrder, mechanic);
        activeOrder.startProgress();

        ServiceOrder otherOrder = createApprovedOrder(
                carService,
                OTHER_ACTIVE_ORDER_ID,
                car,
                createEngineWork());
        try {
            carService.assignMechanicToOrder(otherOrder, mechanic);
        } catch (DomainException exception) {
            printExpectedError(BUSY_MECHANIC_TEST, exception);
        }
    }

    private static void demonstrateCompletedOrderModificationRejection(ServiceOrder order) {
        // This verifies that completed orders cannot be modified.
        try {
            order.addWork(createEngineWork());
        } catch (DomainException exception) {
            printExpectedError(COMPLETED_ORDER_TEST, exception);
        }
    }

    private static void demonstratePriorityCostCalculation(Car car, CarService carService) {
        ServiceOrder normalOrder = createApprovedOrder(
                carService,
                NORMAL_PRIORITY_ORDER_ID,
                car,
                createEngineWork(),
                createBrakesWork());
        ServiceOrder urgentOrder = createApprovedOrder(
                carService,
                URGENT_PRIORITY_ORDER_ID,
                car,
                createEngineWork(),
                createBrakesWork());
        urgentOrder.setPriority(OrderPriority.URGENT);
        System.out.println(String.format(
                PRIORITY_TOTAL_COST_FORMAT,
                NORMAL_PRIORITY_LABEL,
                normalOrder.calculateTotalCost()));
        System.out.println(String.format(
                PRIORITY_TOTAL_COST_FORMAT,
                URGENT_PRIORITY_LABEL,
                urgentOrder.calculateTotalCost()));
    }

    private static void demonstrateGiftTiers(Car car, CarService carService) {
        ServiceOrder interiorCleaningOrder = createApprovedOrder(
                carService,
                INTERIOR_CLEANING_GIFT_ORDER_ID,
                car,
                createGiftWork(INTERIOR_CLEANING_PARTS_TOTAL_VALUE));
        ServiceOrder discountVoucherOrder = createApprovedOrder(
                carService,
                DISCOUNT_VOUCHER_GIFT_ORDER_ID,
                car,
                createGiftWork(DISCOUNT_VOUCHER_PARTS_TOTAL_VALUE));
        printGiftScenario(interiorCleaningOrder);
        printGiftScenario(discountVoucherOrder);
    }

    private static ServiceWork createGiftWork(String partsCostValue) {
        return new ServiceWork(
                GIFT_WORK_DESCRIPTION,
                new BigDecimal(partsCostValue),
                new BigDecimal(GIFT_WORK_LABOR_COST_VALUE),
                MechanicSpecialization.GENERAL);
    }

    private static void printGiftScenario(ServiceOrder order) {
        String giftName = order.getPresent().getGift()
                .map(gift -> gift.getName())
                .orElse(NO_GIFT_LABEL);
        System.out.println(String.format(
                GIFT_SCENARIO_FORMAT,
                order.calculatePartsTotal(),
                giftName));
    }

    private static ServiceOrder createApprovedOrder(
            CarService carService,
            String orderId,
            Car car,
            ServiceWork... works) {
        ServiceOrder order = new ServiceOrder(orderId, car);
        for (ServiceWork work : works) {
            order.addWork(work);
        }
        carService.registerOrder(order);
        order.diagnose();
        order.approve();
        return order;
    }

    private static ServiceWork createEngineWork() {
        return new ServiceWork(
                ENGINE_WORK_DESCRIPTION,
                ENGINE_PARTS_COST,
                ENGINE_LABOR_COST,
                MechanicSpecialization.ENGINE);
    }

    private static ServiceWork createBrakesWork() {
        return new ServiceWork(
                BRAKES_WORK_DESCRIPTION,
                BRAKES_PARTS_COST,
                BRAKES_LABOR_COST,
                MechanicSpecialization.BRAKES);
    }

    private static void completeWorks(ServiceOrder order) {
        for (ServiceWork work : order.getWorks()) {
            work.complete();
        }
    }

    private static void printExpectedError(String scenario, DomainException exception) {
        System.out.println(String.format(EXPECTED_ERROR_FORMAT, scenario, exception.getMessage()));
    }
}
