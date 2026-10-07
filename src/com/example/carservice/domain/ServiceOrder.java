package com.example.carservice.domain;

import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.InvalidStateTransitionException;
import com.example.carservice.exception.MechanicNotCompatibleException;
import com.example.carservice.exception.OrderModificationNotAllowedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceOrder {
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

    public void addWork(ServiceWork work) {
        ensureModifiable();
        works.add(work);
    }

    public boolean removeWork(ServiceWork work) {
        ensureModifiable();
        return works.remove(work);
    }

    public void assignMechanic(Mechanic mechanic) {
        ensureModifiable();
        if (!isCompatible(mechanic)) {
            throw new MechanicNotCompatibleException(DomainMessages.mechanicNotCompatible);
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
            throw new InvalidStateTransitionException(DomainMessages.invalidDiagnoseStatus);
        }
        status = OrderStatus.DIAGNOSED;
    }

    public void approve() {
        if (status != OrderStatus.DIAGNOSED) {
            throw new InvalidStateTransitionException(DomainMessages.invalidApproveStatus);
        }
        // Approval requires at least one work item.
        if (works.isEmpty()) {
            throw new InvalidStateTransitionException(DomainMessages.emptyOrder);
        }
        status = OrderStatus.APPROVED;
    }

    public void startProgress() {
        if (status != OrderStatus.APPROVED) {
            throw new InvalidStateTransitionException(DomainMessages.invalidProgressStatus);
        }
        // A mechanic must be assigned before work can begin.
        if (assignedMechanic == null) {
            throw new InvalidStateTransitionException(DomainMessages.mechanicRequired);
        }
        status = OrderStatus.IN_PROGRESS;
    }

    public void complete() {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new InvalidStateTransitionException(DomainMessages.invalidCompleteStatus);
        }
        if (works.stream().anyMatch(work -> work.getStatus() != WorkStatus.COMPLETED)) {
            throw new InvalidStateTransitionException(DomainMessages.unfinishedWork);
        }
        status = OrderStatus.COMPLETED;
        assignedMechanic.releaseFromOrder();
    }

    public void cancel() {
        if (status != OrderStatus.CREATED
                && status != OrderStatus.DIAGNOSED
                && status != OrderStatus.APPROVED) {
            throw new InvalidStateTransitionException(DomainMessages.invalidCancelStatus + status);
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
            throw new OrderModificationNotAllowedException(DomainMessages.closedOrder);
        }
    }

    private boolean isClosed() {
        return status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED;
    }
}
