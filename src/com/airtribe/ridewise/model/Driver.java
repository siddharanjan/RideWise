package com.airtribe.ridewise.model;

import java.util.Objects;

public class Driver {

    private final String id;
    private final String name;
    private final VehicleType vehicleType;
    private Location currentLocation;
    private boolean available;
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
            throw new IllegalStateException("Driver " + id + " is already on a trip");
        }
        available = false;
    }

    public void finishTrip() {
        if (available) {
            throw new IllegalStateException("Driver " + id + " is not on a trip");
        }
        completedTrips++;
        available = true;
    }

    // used when a ride gets cancelled before it starts - driver goes back
    // online but this doesn't count as a completed trip
    public void abortTrip() {
        available = true;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return String.format("%s %s (%s) at %s - %s, %d trips",
                id, name, vehicleType, currentLocation,
                available ? "available" : "on trip", completedTrips);
    }
}
