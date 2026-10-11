package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Rider;

import java.util.Comparator;
import java.util.List;

// picks whichever available driver is physically closest to the rider
public class NearestDriverStrategy implements RideMatchingStrategy {

    @Override
    public Driver findDriver(Rider rider, List<Driver> drivers) {
        if (rider == null || drivers == null || drivers.isEmpty()) {
            return null;
        }
        return drivers.stream()
                .filter(Driver::isAvailable)
                .min(Comparator.comparingDouble((Driver driver) -> driver.distanceFrom(rider.getLocation()))
                        .thenComparing(Driver::getId))
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Nearest driver";
    }
}
