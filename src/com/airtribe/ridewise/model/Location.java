package com.airtribe.ridewise.model;

// just an (x, y) point on a grid - used for the nearest-driver distance calc
public final class Location {

    private final double x;
    private final double y;

    public Location(double x, double y) {
        requireFinite(x, "x");
        requireFinite(y, "y");
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double distanceTo(Location other) {
        if (other == null) {
            throw new IllegalArgumentException("other location is required");
        }
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private static void requireFinite(double value, String axis) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(axis + " must be a finite number: " + value);
        }
    }

    @Override
    public String toString() {
        return String.format("(%.1f, %.1f)", x, y);
    }
}
