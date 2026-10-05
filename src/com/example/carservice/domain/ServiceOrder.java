package com.example.carservice.domain;

import com.example.carservice.exception.InvalidArgumentException;
import com.example.carservice.exception.InvalidStateTransitionException;
import com.example.carservice.exception.MechanicNotCompatibleException;
import com.example.carservice.exception.OrderModificationNotAllowedException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceOrder {
    private static BigDecimal URGENT_LABOR_SURCHARGE_FACTOR = new BigDecimal("1.20");
    private static String INVALID_DIAGNOSE_STATUS_MESSAGE = "Only created orders can be diagnosed.";
    private static String INVALID_APPROVE_STATUS_MESSAGE = "Only diagnosed orders can be approved.";
    private static String EMPTY_ORDER_MESSAGE = "An order cannot be approved without service work.";
    private static String INVALID_PROGRESS_STATUS_MESSAGE = "Only approved orders can start progress.";
    private static String MECHANIC_REQUIRED_MESSAGE = "A mechanic must be assigned before work starts.";
    private static String MECHANIC_REQUIRED_FOR_ASSIGNMENT_MESSAGE = "A mechanic is required for assignment.";
    private static String MECHANIC_NOT_COMPATIBLE_MESSAGE = "The mechanic is not compatible with the order work.";
    private static String INVALID_COMPLETE_STATUS_MESSAGE = "Only in-progress orders can be completed.";
    private static String UNFINISHED_WORK_MESSAGE = "An order cannot be completed with unfinished work.";
    private static String CLOSED_ORDER_MESSAGE = "A completed or cancelled order cannot be modified.";
    private static String INVALID_CANCEL_STATUS_MESSAGE = "Cannot cancel order in status: ";
    private static String WORK_REQUIRED_MESSAGE = "Service work is required.";

    private String id;
    private Car car;
    private List<ServiceWork> works;
    private Mechanic assignedMechanic;
    private OrderStatus status;
    private OrderPriority priority;

    public ServiceOrder(String id, Car car) {
        this.id = id;
        this.car = car;
        this.works = new ArrayList<>();
        this.status = OrderStatus.CREATED;
        this.priority = OrderPriority.NORMAL;
    }

    public String getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public List<ServiceWork> getWorks() {
        return Collections.unmodifiableList(works);
    }

    public Mechanic getAssignedMechanic() {
        return assignedMechanic;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public OrderPriority getPriority() {
        return priority;
    }

    public void setPriority(OrderPriority priority) {
        this.priority = priority;
    }

    public BigDecimal calculateTotalCost() {
        BigDecimal partsCost = BigDecimal.ZERO;
        BigDecimal laborCost = BigDecimal.ZERO;
        for (ServiceWork work : works) {
            partsCost = partsCost.add(work.getPartsCost());
            laborCost = laborCost.add(work.getLaborCost());
        }
        if (priority == OrderPriority.URGENT) {
            laborCost = laborCost.multiply(URGENT_LABOR_SURCHARGE_FACTOR);
        }
        return partsCost.add(laborCost);
    }

    public void addWork(ServiceWork work) {
        ensureModifiable();
        if (work == null) {
            throw new InvalidArgumentException(WORK_REQUIRED_MESSAGE);
        }
        works.add(work);
    }

    public boolean removeWork(ServiceWork work) {
        ensureModifiable();
        return works.remove(work);
    }

    public void assignMechanic(Mechanic mechanic) {
        ensureModifiable();
        if (mechanic == null) {
            throw new InvalidArgumentException(MECHANIC_REQUIRED_FOR_ASSIGNMENT_MESSAGE);
        }
        if (!isCompatible(mechanic)) {
            throw new MechanicNotCompatibleException(MECHANIC_NOT_COMPATIBLE_MESSAGE);
        }
        if (mechanic == assignedMechanic) {
            return;
        }
        mechanic.assignToOrder();
        if (assignedMechanic != null) {
            assignedMechanic.releaseFromOrder();
        }
        assignedMechanic = mechanic;
    }

    public void diagnose() {
        if (status != OrderStatus.CREATED) {
            throw new InvalidStateTransitionException(INVALID_DIAGNOSE_STATUS_MESSAGE);
        }
        status = OrderStatus.DIAGNOSED;
    }

    public void approve() {
        if (status != OrderStatus.DIAGNOSED) {
            throw new InvalidStateTransitionException(INVALID_APPROVE_STATUS_MESSAGE);
        }
        // Approval requires at least one work item.
        if (works.isEmpty()) {
            throw new InvalidStateTransitionException(EMPTY_ORDER_MESSAGE);
        }
        status = OrderStatus.APPROVED;
    }

    public void startProgress() {
        if (status != OrderStatus.APPROVED) {
            throw new InvalidStateTransitionException(INVALID_PROGRESS_STATUS_MESSAGE);
        }
        // A mechanic must be assigned before work can begin.
        if (assignedMechanic == null) {
            throw new InvalidStateTransitionException(MECHANIC_REQUIRED_MESSAGE);
        }
        status = OrderStatus.IN_PROGRESS;
    }

    public void complete() {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new InvalidStateTransitionException(INVALID_COMPLETE_STATUS_MESSAGE);
        }
        if (works.stream().anyMatch(work -> work.getStatus() != WorkStatus.COMPLETED)) {
            throw new InvalidStateTransitionException(UNFINISHED_WORK_MESSAGE);
        }
        status = OrderStatus.COMPLETED;
        assignedMechanic.releaseFromOrder();
    }

    public void cancel() {
        if (status != OrderStatus.CREATED
                && status != OrderStatus.DIAGNOSED
                && status != OrderStatus.APPROVED) {
            throw new InvalidStateTransitionException(INVALID_CANCEL_STATUS_MESSAGE + status);
        }
        status = OrderStatus.CANCELLED;
        if (assignedMechanic != null) {
            assignedMechanic.releaseFromOrder();
        }
    }

    private boolean isCompatible(Mechanic mechanic) {
        return works.stream().allMatch(work ->
                work.getRequiredSpecialization() == MechanicSpecialization.GENERAL
                        || mechanic.getSpecialization() == MechanicSpecialization.GENERAL
                        || work.getRequiredSpecialization() == mechanic.getSpecialization());
    }

    private void ensureModifiable() {
        // Completed and cancelled orders cannot be changed.
        if (isClosed()) {
            throw new OrderModificationNotAllowedException(CLOSED_ORDER_MESSAGE);
        }
    }

    private boolean isClosed() {
        return status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED;
    }
}
