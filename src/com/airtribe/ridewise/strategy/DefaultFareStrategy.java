package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.VehicleType;

import java.util.Objects;

// base fare + distance * rate, nothing fancy
public class DefaultFareStrategy implements FareStrategy {

    @Override
    public double calculateFare(Ride ride) {
        Objects.requireNonNull(ride, "ride is required");
        VehicleType vehicleType = ride.getVehicleType();
        return vehicleType.getBaseFare() + (vehicleType.getRatePerKm() * ride.getDistanceInKm());
    }

    @Override
    public String toString() {
        return "Default fare";
    }
}
