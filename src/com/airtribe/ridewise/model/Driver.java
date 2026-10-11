package com.airtribe.ridewise.model;

import java.util.Objects;

public class Driver {

    private final String id;
    private final String name;
    private final VehicleType vehicleType;
    private Location currentLocation;
    private boolean available;
    private boolean onTrip;
    private int completedTrips;

    public Driver(String id, String name, VehicleType vehicleType, Location currentLocation) {
        this.id = Objects.requireNonNull(id, "driver id is required");
        this.name = Objects.requireNonNull(name, "driver name is required");
        this.vehicleType = Objects.requireNonNull(vehicleType, "vehicle type is required");
        this.currentLocation = Objects.requireNonNull(currentLocation, "driver location is required");
        this.available = true;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getCompletedTrips() {
        return completedTrips;
    }

    public double distanceFrom(Location other) {
        return currentLocation.distanceTo(other);
    }

    public void goOnTrip() {
        if (!available) {
            throw new IllegalStateException("Driver " + id + " cannot be booked right now");
        }
        available = false;
        onTrip = true;
    }

    public void finishTrip() {
        if (!onTrip) {
            throw new IllegalStateException("Driver " + id + " is not on a trip");
        }
        completedTrips++;
        available = true;
        onTrip = false;
    }

    // used when a ride gets cancelled before it starts - driver goes back
    // online but this doesn't count as a completed trip
    public void abortTrip() {
        if (!onTrip) {
            throw new IllegalStateException("Driver " + id + " is not on a trip");
        }
        available = true;
        onTrip = false;
    }

    // manual duty toggle. blocked while on a trip so nobody can flip a busy
    // driver back to "available" and get them double-booked
    public void setAvailable(boolean available) {
        if (onTrip) {
            throw new IllegalStateException("Driver " + id + " is on a trip, cannot change duty status");
        }
        this.available = available;
    }

    @Override
    public String toString() {
        String status = onTrip ? "on trip" : (available ? "available" : "off duty");
        return String.format("%s %s (%s) at %s - %s, %d trips",
                id, name, vehicleType, currentLocation, status, completedTrips);
    }
}
