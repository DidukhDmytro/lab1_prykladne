package com.example.carservice.exception;

/**
 * Signals that a requested service schedule conflicts with an existing booking.
 */
public class ScheduleConflictException extends DomainException {
    public ScheduleConflictException(String message) {
        super(message);
    }
}
