package com.airtribe.ridewise.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

// generated once a ride completes, holds what the rider got charged
public final class FareReceipt {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    private final String rideId;
    private final double amount;
    private final LocalDateTime generatedAt;

    public FareReceipt(String rideId, double amount, LocalDateTime generatedAt) {
        if (amount < 0) {
            throw new IllegalArgumentException("Fare cannot be negative: " + amount);
        }
        this.rideId = Objects.requireNonNull(rideId, "ride id is required");
        this.amount = amount;
        this.generatedAt = Objects.requireNonNull(generatedAt, "timestamp is required");
    }

    public String getRideId() {
        return rideId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    @Override
    public String toString() {
        return String.format("Receipt for %s: Rs. %.2f (generated %s)",
                rideId, amount, generatedAt.format(TIMESTAMP));
    }
}
