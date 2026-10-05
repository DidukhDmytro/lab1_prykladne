package com.example.carservice.domain;

import com.example.carservice.exception.MechanicAlreadyAssignedException;

public class Mechanic {
    private static String ALREADY_ASSIGNED_MESSAGE = "Mechanic is already assigned to an order.";

    private String id;
    private String fullName;
    private MechanicSpecialization specialization;
    private boolean busy;

    public Mechanic(String id, String fullName, MechanicSpecialization specialization, boolean busy) {
        this.id = id;
        this.fullName = fullName;
        this.specialization = specialization;
        this.busy = busy;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public MechanicSpecialization getSpecialization() {
        return specialization;
    }

    public boolean isBusy() {
        return busy;
    }

    public void assignToOrder() {
        if (busy) {
            throw new MechanicAlreadyAssignedException(ALREADY_ASSIGNED_MESSAGE);
        }
        busy = true;
    }

    public void releaseFromOrder() {
        busy = false;
    }
}
