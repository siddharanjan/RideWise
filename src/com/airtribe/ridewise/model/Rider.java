package com.airtribe.ridewise.model;

import java.util.Objects;

public class Rider {

    private final String id;
    private final String name;
    private Location location;

    public Rider(String id, String name, Location location) {
        this.id = Objects.requireNonNull(id, "rider id is required");
        this.name = Objects.requireNonNull(name, "rider name is required");
        this.location = Objects.requireNonNull(location, "rider location is required");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public void moveTo(Location newLocation) {
        this.location = Objects.requireNonNull(newLocation, "new location is required");
    }

    @Override
    public String toString() {
        return String.format("%s %s at %s", id, name, location);
    }
}
