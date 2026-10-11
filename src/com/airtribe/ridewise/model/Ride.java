package com.airtribe.ridewise.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * One booking, from request through to completion or cancellation.
 * Keeps track of its own status so a driver can't be assigned twice,
 * and a ride can't be completed before it has one.
 */
public class Ride {

    private final String id;
    private final Rider rider;
    private final VehicleType vehicleType;
    private final double distanceInKm;
    private final LocalDateTime requestedAt;

    private Driver driver;
    private RideStatus status;
    private FareReceipt receipt;
    private String cancellationReason;

    public Ride(String id, Rider rider, VehicleType vehicleType, double distanceInKm) {
        if (distanceInKm <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero: " + distanceInKm);
        }
        this.id = Objects.requireNonNull(id, "ride id is required");
        this.rider = Objects.requireNonNull(rider, "rider is required");
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicle type is required");
        this.distanceInKm = distanceInKm;
        this.requestedAt = LocalDateTime.now();
        this.status = RideStatus.REQUESTED;
    }

    public String getId() {
        return id;
    }

    public Rider getRider() {
        return rider;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public double getDistanceInKm() {
        return distanceInKm;
    }

    public RideStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public Optional<Driver> getDriver() {
        return Optional.ofNullable(driver);
    }

    public Optional<FareReceipt> getReceipt() {
        return Optional.ofNullable(receipt);
    }

    public Optional<String> getCancellationReason() {
        return Optional.ofNullable(cancellationReason);
    }

    public void assignTo(Driver driver) {
        requireStatus(RideStatus.REQUESTED, "assign a driver");
        this.driver = Objects.requireNonNull(driver, "driver is required");
        this.status = RideStatus.ASSIGNED;
    }

    // fare comes from whatever FareStrategy the service is using, this just
    // records it and generates the receipt
    public FareReceipt complete(double fare) {
        requireStatus(RideStatus.ASSIGNED, "complete the ride");
        this.receipt = new FareReceipt(id, fare, LocalDateTime.now());
        this.status = RideStatus.COMPLETED;
        return receipt;
    }

    public void cancel(String reason) {
        if (status == RideStatus.COMPLETED || status == RideStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Ride " + id + " is already " + status + " and cannot be cancelled");
        }
        this.cancellationReason = reason;
        this.status = RideStatus.CANCELLED;
    }

    private void requireStatus(RideStatus expected, String action) {
        if (status != expected) {
            throw new IllegalStateException(String.format(
                    "Cannot %s for ride %s: expected status %s but was %s",
                    action, id, expected, status));
        }
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %.1f km | %s | %s",
                id,
                rider.getName(),
                driver == null ? "unassigned" : driver.getName(),
                distanceInKm,
                vehicleType,
                status);
    }
}
