package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Ride;

import java.time.Clock;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Wraps another FareStrategy and multiplies the result during rush hour
 * (8-11am and 5-9pm). Wrapping instead of subclassing means this works on
 * top of whatever base strategy is in use.
 *
 * Clock is injected so tests don't have to actually run during rush hour.
 */
public class PeakHourFareStrategy implements FareStrategy {

    private static final LocalTime MORNING_PEAK_START = LocalTime.of(8, 0);
    private static final LocalTime MORNING_PEAK_END = LocalTime.of(11, 0);
    private static final LocalTime EVENING_PEAK_START = LocalTime.of(17, 0);
    private static final LocalTime EVENING_PEAK_END = LocalTime.of(21, 0);

    private final FareStrategy baseStrategy;
    private final double surgeMultiplier;
    private final Clock clock;

    public PeakHourFareStrategy(FareStrategy baseStrategy, double surgeMultiplier) {
        this(baseStrategy, surgeMultiplier, Clock.systemDefaultZone());
    }

    public PeakHourFareStrategy(FareStrategy baseStrategy, double surgeMultiplier, Clock clock) {
        if (surgeMultiplier < 1.0) {
            throw new IllegalArgumentException("Surge multiplier cannot be below 1.0: " + surgeMultiplier);
        }
        this.baseStrategy = Objects.requireNonNull(baseStrategy, "base fare strategy is required");
        this.surgeMultiplier = surgeMultiplier;
        this.clock = Objects.requireNonNull(clock, "clock is required");
    }

    @Override
    public double calculateFare(Ride ride) {
        double fare = baseStrategy.calculateFare(ride);
        return isPeakHour() ? fare * surgeMultiplier : fare;
    }

    public boolean isPeakHour() {
        LocalTime now = LocalTime.now(clock);
        return within(now, MORNING_PEAK_START, MORNING_PEAK_END)
                || within(now, EVENING_PEAK_START, EVENING_PEAK_END);
    }

    private boolean within(LocalTime time, LocalTime start, LocalTime end) {
        return !time.isBefore(start) && time.isBefore(end);
    }

    @Override
    public String toString() {
        return String.format("Peak hour surge x%.1f over [%s]", surgeMultiplier, baseStrategy);
    }
}
