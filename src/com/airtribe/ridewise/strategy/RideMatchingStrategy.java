package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Rider;

import java.util.List;

/**
 * Picks a driver for a ride out of the candidates passed in.
 * Returns null if nobody in the list can take it.
 */
public interface RideMatchingStrategy {
    Driver findDriver(Rider rider, List<Driver> drivers);
}
