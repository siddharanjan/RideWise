package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Ride;

// any pricing rule implements this. calculateFare should just read the
// ride and return a number - no side effects, no touching the receipt
public interface FareStrategy {
    double calculateFare(Ride ride);
}
