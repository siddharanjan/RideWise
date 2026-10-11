package com.airtribe.ridewise.service;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.FareReceipt;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;
import com.airtribe.ridewise.util.IdGenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handles the ride lifecycle - request, assign, complete, cancel.
 * Matching and pricing are both passed in through the constructor so
 * swapping either one out doesn't need any change here.
 */
public class RideService {

    private final DriverService driverService;
    private final RideMatchingStrategy matchingStrategy;
    private final FareStrategy fareStrategy;

    private final Map<String, Ride> rides = new LinkedHashMap<>();
    private final IdGenerator idGenerator = new IdGenerator("RIDE");

    public RideService(DriverService driverService,
                       RideMatchingStrategy matchingStrategy,
                       FareStrategy fareStrategy) {
        this.driverService = Objects.requireNonNull(driverService, "driver service is required");
        this.matchingStrategy = Objects.requireNonNull(matchingStrategy, "matching strategy is required");
        this.fareStrategy = Objects.requireNonNull(fareStrategy, "fare strategy is required");
    }

    public Ride requestRide(Rider rider, VehicleType vehicleType, double distanceInKm) {
        Objects.requireNonNull(rider, "rider is required");
        Ride ride = new Ride(idGenerator.next(), rider, vehicleType, distanceInKm);
        rides.put(ride.getId(), ride);

        List<Driver> candidates = driverService.getAvailableDrivers(vehicleType);
        Driver driver = matchingStrategy.findDriver(rider, candidates);
        if (driver == null) {
            // nobody free right now - record it as cancelled instead of
            // just throwing and losing the request entirely
            ride.cancel("No " + vehicleType + " driver available");
            throw new NoDriverAvailableException(
                    "No " + vehicleType + " driver available right now - ride " + ride.getId() + " was cancelled");
        }

        ride.assignTo(driver);
        driverService.bookForTrip(driver);
        return ride;
    }

    public FareReceipt completeRide(String rideId) {
        Ride ride = getRide(rideId);
        Driver driver = ride.getDriver().orElseThrow(() -> new IllegalStateException(
                "Ride " + ride.getId() + " has no driver assigned yet"));

        double fare = fareStrategy.calculateFare(ride);
        FareReceipt receipt = ride.complete(fare);
        driverService.releaseAfterTrip(driver);
        return receipt;
    }

    public Ride cancelRide(String rideId, String reason) {
        Ride ride = getRide(rideId);
        ride.getDriver().ifPresent(driverService::releaseWithoutTrip);
        ride.cancel(reason);
        return ride;
    }

    public Ride getRide(String rideId) {
        Ride ride = rides.get(IdGenerator.normalise(rideId));
        if (ride == null) {
            throw new IllegalArgumentException("No ride found with id " + rideId);
        }
        return ride;
    }

    public Collection<Ride> getAllRides() {
        return Collections.unmodifiableCollection(rides.values());
    }
}
