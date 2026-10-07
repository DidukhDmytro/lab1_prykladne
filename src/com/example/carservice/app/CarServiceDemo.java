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
    private static String customerId = "customer-001";
    private static String customerName = "Alex Morgan";
    private static String customerPhone = "+1-555-0100";
    private static String carVin = "DEMO-VIN-001";
    private static String carMake = "Example Motors";
    private static String carModel = "Roadster";
    private static int carYear = 2024;
    private static String generalMechanicId = "mechanic-general";
    private static String engineMechanicId = "mechanic-engine";
    private static String brakesMechanicId = "mechanic-brakes";
    private static String generalMechanicName = "Taylor General";
    private static String engineMechanicName = "Jordan Engine";
    private static String brakesMechanicName = "Casey Brakes";
    private static boolean mechanicInitialBusy = false;
    private static String positiveOrderId = "order-positive";
    private static String engineWorkDescription = "Engine service";
    private static String brakesWorkDescription = "Brake service";
    private static String enginePartsCostValue = "200.00";
    private static String engineLaborCostValue = "85.00";
    private static String brakesPartsCostValue = "60.00";
    private static String brakesLaborCostValue = "45.00";
    private static BigDecimal enginePartsCost = new BigDecimal(enginePartsCostValue);
    private static BigDecimal engineLaborCost = new BigDecimal(engineLaborCostValue);
    private static BigDecimal brakesPartsCost = new BigDecimal(brakesPartsCostValue);
    private static BigDecimal brakesLaborCost = new BigDecimal(brakesLaborCostValue);
    private static String emptyOrderId = "order-empty";
    private static String incompatibleOrderId = "order-incompatible";
    private static String busyOrderId = "order-busy";
    private static String otherActiveOrderId = "order-other-active";
    private static String normalPriorityOrderId = "order-normal-priority";
    private static String urgentPriorityOrderId = "order-urgent-priority";
    private static String invalidTransitionOrderId = "order-invalid-transition";
    private static String washerFluidGiftOrderId = "order-washer-fluid-gift";
    private static String interiorCleaningGiftOrderId = "order-interior-cleaning-gift";
    private static String discountVoucherGiftOrderId = "order-discount-voucher-gift";
    private static String giftWorkDescription = "Parts order";
    private static String washerFluidPartsTotalValue = "250.00";
    private static String interiorCleaningPartsTotalValue = "550.00";
    private static String discountVoucherPartsTotalValue = "1111.00";
    private static String giftWorkLaborCostValue = "1.00";
    private static String duplicateVinTest = "Duplicate VIN registration";
    private static String emptyOrderTest = "Approving an empty order";
    private static String incompatibleMechanicTest = "Assigning an incompatible mechanic";
    private static String busyMechanicTest = "Assigning a busy mechanic";
    private static String completedOrderTest = "Modifying a completed order";
    private static String positiveFlowMessage = "Positive order flow completed.";
    private static String totalCostFormat = "Total order cost: %.2f";
    private static String priorityTotalCostFormat = "%s order total cost: %.2f";
    private static String normalPriorityLabel = "Normal";
    private static String urgentPriorityLabel = "Urgent";
    private static String expectedErrorFormat = "%s: expected error - %s";
    private static String invalidTransitionErrorFormat =
            "Invalid status transition check: expected error -> %s";
    private static String giftScenarioFormat = "Parts total: %.2f; gift: %s";
    private static String noGiftLabel = "No gift";

    private CarServiceDemo() {
    }

    public static void main(String[] args) {
        CarService carService = new CarService();
        Customer customer = carService.createCustomer(customerId, customerName, customerPhone);
        Car car = carService.createCar(carVin, carMake, carModel, carYear, customer);
        Mechanic generalMechanic = new Mechanic(
                generalMechanicId,
                generalMechanicName,
                MechanicSpecialization.GENERAL,
                mechanicInitialBusy);
        Mechanic engineMechanic = new Mechanic(
                engineMechanicId,
                engineMechanicName,
                MechanicSpecialization.ENGINE,
                mechanicInitialBusy);
        Mechanic brakesMechanic = new Mechanic(
                brakesMechanicId,
                brakesMechanicName,
                MechanicSpecialization.BRAKES,
                mechanicInitialBusy);

        carService.registerCustomer(customer);
        carService.registerCar(car);
        carService.registerMechanic(generalMechanic);
        carService.registerMechanic(engineMechanic);
        carService.registerMechanic(brakesMechanic);

        ServiceOrder positiveOrder = createApprovedOrder(
                carService,
                positiveOrderId,
                car,
                createEngineWork(carService),
                createBrakesWork(carService));
        carService.assignMechanicToOrder(positiveOrder, generalMechanic);
        positiveOrder.startProgress();
        completeWorks(positiveOrder);
        positiveOrder.complete();
        System.out.println(positiveFlowMessage);
        System.out.println(String.format(totalCostFormat, carService.calculateTotalCost(positiveOrder)));
        demonstrateGiftTiers(car, carService);
        demonstratePriorityCostCalculation(car, carService);

        demonstrateDuplicateVinRejection(carService, customer);
        demonstrateEmptyOrderApprovalRejection(carService, car);
        demonstrateIncompatibleMechanicRejection(carService, car, brakesMechanic);
        demonstrateBusyMechanicRejection(carService, car, engineMechanic);
        demonstrateCompletedOrderModificationRejection(positiveOrder, carService);

        ServiceOrder invalidTransitionOrder = new ServiceOrder(invalidTransitionOrderId, car);
        try {
            invalidTransitionOrder.complete();
        } catch (InvalidStateTransitionException exception) {
            System.out.println(String.format(
                    invalidTransitionErrorFormat,
                    exception.getMessage()));
        }
    }

    private static void demonstrateDuplicateVinRejection(CarService carService, Customer customer) {
        // This verifies that registered VINs cannot be reused.
        try {
            carService.registerCar(carService.createCar(carVin, carMake, carModel, carYear, customer));
        } catch (DomainException exception) {
            printExpectedError(duplicateVinTest, exception);
        }
    }

    private static void demonstrateEmptyOrderApprovalRejection(CarService carService, Car car) {
        // This verifies that approval requires at least one work item.
        ServiceOrder order = new ServiceOrder(emptyOrderId, car);
        carService.registerOrder(order);
        order.diagnose();
        try {
            order.approve();
        } catch (DomainException exception) {
            printExpectedError(emptyOrderTest, exception);
        }
    }

    private static void demonstrateIncompatibleMechanicRejection(
            CarService carService,
            Car car,
            Mechanic mechanic) {
        // This verifies that every work specialization must match the mechanic.
        ServiceOrder order = createApprovedOrder(
                carService,
                incompatibleOrderId,
                car,
                createEngineWork(carService));
        try {
            carService.assignMechanicToOrder(order, mechanic);
        } catch (DomainException exception) {
            printExpectedError(incompatibleMechanicTest, exception);
        }
    }

    private static void demonstrateBusyMechanicRejection(
            CarService carService,
            Car car,
            Mechanic mechanic) {
        // This verifies that a mechanic assigned to active work cannot be reused.
        ServiceOrder activeOrder = createApprovedOrder(
                carService,
                busyOrderId,
                car,
                createEngineWork(carService));
        carService.assignMechanicToOrder(activeOrder, mechanic);
        activeOrder.startProgress();

        ServiceOrder otherOrder = createApprovedOrder(
                carService,
                otherActiveOrderId,
                car,
                createEngineWork(carService));
        try {
            carService.assignMechanicToOrder(otherOrder, mechanic);
        } catch (DomainException exception) {
            printExpectedError(busyMechanicTest, exception);
        }
    }

    private static void demonstrateCompletedOrderModificationRejection(
            ServiceOrder order,
            CarService carService) {
        // This verifies that completed orders cannot be modified.
        try {
            carService.addWork(order, createEngineWork(carService));
        } catch (DomainException exception) {
            printExpectedError(completedOrderTest, exception);
        }
    }

    private static void demonstratePriorityCostCalculation(Car car, CarService carService) {
        ServiceOrder normalOrder = createApprovedOrder(
                carService,
                normalPriorityOrderId,
                car,
                createEngineWork(carService),
                createBrakesWork(carService));
        ServiceOrder urgentOrder = createApprovedOrder(
                carService,
                urgentPriorityOrderId,
                car,
                createEngineWork(carService),
                createBrakesWork(carService));
        urgentOrder.setPriority(OrderPriority.URGENT);
        System.out.println(String.format(
                priorityTotalCostFormat,
                normalPriorityLabel,
                carService.calculateTotalCost(normalOrder)));
        System.out.println(String.format(
                priorityTotalCostFormat,
                urgentPriorityLabel,
                carService.calculateTotalCost(urgentOrder)));
    }

    private static void demonstrateGiftTiers(Car car, CarService carService) {
        ServiceOrder washerFluidOrder = createApprovedOrder(
                carService,
                washerFluidGiftOrderId,
                car,
                createGiftWork(carService, washerFluidPartsTotalValue));
        ServiceOrder interiorCleaningOrder = createApprovedOrder(
                carService,
                interiorCleaningGiftOrderId,
                car,
                createGiftWork(carService, interiorCleaningPartsTotalValue));
        ServiceOrder discountVoucherOrder = createApprovedOrder(
                carService,
                discountVoucherGiftOrderId,
                car,
                createGiftWork(carService, discountVoucherPartsTotalValue));
        printGiftScenario(washerFluidOrder, carService);
        printGiftScenario(interiorCleaningOrder, carService);
        printGiftScenario(discountVoucherOrder, carService);
    }

    private static ServiceWork createGiftWork(CarService carService, String partsCostValue) {
        return carService.createServiceWork(
                giftWorkDescription,
                new BigDecimal(partsCostValue),
                new BigDecimal(giftWorkLaborCostValue),
                MechanicSpecialization.GENERAL);
    }

    private static void printGiftScenario(ServiceOrder order, CarService carService) {
        String giftName = carService.createPresent(order).getGift()
                .map(gift -> gift.getName())
                .orElse(noGiftLabel);
        System.out.println(String.format(
                giftScenarioFormat,
                carService.calculatePartsTotal(order),
                giftName));
    }

    private static ServiceOrder createApprovedOrder(
            CarService carService,
            String orderId,
            Car car,
            ServiceWork... works) {
        ServiceOrder order = new ServiceOrder(orderId, car);
        for (ServiceWork work : works) {
            carService.addWork(order, work);
        }
        carService.registerOrder(order);
        order.diagnose();
        order.approve();
        return order;
    }

    private static ServiceWork createEngineWork(CarService carService) {
        return carService.createServiceWork(
                engineWorkDescription,
                enginePartsCost,
                engineLaborCost,
                MechanicSpecialization.ENGINE);
    }

    private static ServiceWork createBrakesWork(CarService carService) {
        return carService.createServiceWork(
                brakesWorkDescription,
                brakesPartsCost,
                brakesLaborCost,
                MechanicSpecialization.BRAKES);
    }

    private static void completeWorks(ServiceOrder order) {
        for (ServiceWork work : order.getWorks()) {
            work.complete();
        }
    }

    private static void printExpectedError(String scenario, DomainException exception) {
        System.out.println(String.format(expectedErrorFormat, scenario, exception.getMessage()));
    }
}
