package com.example.carservice.domain;

import com.example.carservice.exception.DomainMessages;
import com.example.carservice.exception.MechanicAlreadyAssignedException;

public class Mechanic {
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
            throw new MechanicAlreadyAssignedException(DomainMessages.mechanicAlreadyAssigned);
        }
        busy = true;
    }

    public void releaseFromOrder() {
        busy = false;
    }
}
