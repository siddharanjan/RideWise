package com.airtribe.ridewise.model;

// base fare + per km rate for each vehicle type. kept as data on the enum
// since it's a property of the vehicle, not something a fare strategy
// should be hardcoding
public enum VehicleType {

    BIKE(15.0, 6.0),
    AUTO(25.0, 9.0),
    CAR(40.0, 13.5);

    private final double baseFare;
    private final double ratePerKm;

    VehicleType(double baseFare, double ratePerKm) {
        this.baseFare = baseFare;
        this.ratePerKm = ratePerKm;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getRatePerKm() {
        return ratePerKm;
    }
}
