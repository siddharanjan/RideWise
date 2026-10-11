package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Rider;

import java.util.Comparator;
import java.util.List;

// ignores distance completely, just gives the ride to whoever has done
// the fewest trips so far, to spread the work around
public class LeastActiveDriverStrategy implements RideMatchingStrategy {

    @Override
    public Driver findDriver(Rider rider, List<Driver> drivers) {
        if (drivers == null || drivers.isEmpty()) {
            return null;
        }
        return drivers.stream()
                .filter(Driver::isAvailable)
                .min(Comparator.comparingInt(Driver::getCompletedTrips)
                        .thenComparing(Driver::getId))
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Least active driver";
    }
}
