package com.airtribe.ridewise.service;

import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.util.IdGenerator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class RiderService {

    private final Map<String, Rider> riders = new LinkedHashMap<>();
    private final IdGenerator idGenerator = new IdGenerator("RD");

    public Rider registerRider(String name, Location location) {
        requireName(name);
        Rider rider = new Rider(idGenerator.next(), name.trim(), location);
        riders.put(rider.getId(), rider);
        return rider;
    }

    public Rider getRider(String riderId) {
        Rider rider = riders.get(IdGenerator.normalise(riderId));
        if (rider == null) {
            throw new IllegalArgumentException("No rider registered with id " + riderId);
        }
        return rider;
    }

    public Collection<Rider> getAllRiders() {
        return Collections.unmodifiableCollection(riders.values());
    }

    private void requireName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider name cannot be empty");
        }
    }
}
