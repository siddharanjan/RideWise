package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.util.IdGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

// keeps track of all registered drivers and who's free right now
public class DriverService {

    private final Map<String, Driver> drivers = new LinkedHashMap<>();
    private final IdGenerator idGenerator = new IdGenerator("DR");

    public Driver registerDriver(String name, VehicleType vehicleType, Location location) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver name cannot be empty");
        }
        Driver driver = new Driver(idGenerator.next(), name.trim(), vehicleType, location);
        drivers.put(driver.getId(), driver);
        return driver;
    }

    public Driver getDriver(String driverId) {
        Driver driver = drivers.get(IdGenerator.normalise(driverId));
        if (driver == null) {
            throw new IllegalArgumentException("No driver registered with id " + driverId);
        }
        return driver;
    }

    public void updateAvailability(String driverId, boolean available) {
        getDriver(driverId).setAvailable(available);
    }

    public List<Driver> getAvailableDrivers() {
        return findDrivers(Driver::isAvailable);
    }

    // this is the candidate list that gets handed to the matching strategy
    public List<Driver> getAvailableDrivers(VehicleType vehicleType) {
        Objects.requireNonNull(vehicleType, "vehicle type is required");
        return findDrivers(driver -> driver.isAvailable() && driver.getVehicleType() == vehicleType);
    }

    public Collection<Driver> getAllDrivers() {
        return Collections.unmodifiableCollection(drivers.values());
    }

    public void bookForTrip(Driver driver) {
        requireKnown(driver).goOnTrip();
    }

    public void releaseAfterTrip(Driver driver) {
        requireKnown(driver).finishTrip();
    }

    public void releaseWithoutTrip(Driver driver) {
        requireKnown(driver).abortTrip();
    }

    private List<Driver> findDrivers(Predicate<Driver> filter) {
        List<Driver> matches = new ArrayList<>();
        for (Driver driver : drivers.values()) {
            if (filter.test(driver)) {
                matches.add(driver);
            }
        }
        return matches;
    }

    private Driver requireKnown(Driver driver) {
        Objects.requireNonNull(driver, "driver is required");
        Driver known = drivers.get(driver.getId());
        if (known != driver) {
            throw new IllegalArgumentException("Driver " + driver.getId() + " is not part of the pool");
        }
        return known;
    }
}
