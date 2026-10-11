package com.airtribe.ridewise.util;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

// hands out ids like RD-1, DR-2, RIDE-3. each service has its own instance
// of this so the counters don't step on each other
public class IdGenerator {

    private final String prefix;
    private final AtomicInteger counter = new AtomicInteger();

    public IdGenerator(String prefix) {
        this.prefix = Objects.requireNonNull(prefix, "prefix is required");
    }

    public String next() {
        return prefix + "-" + counter.incrementAndGet();
    }

    // so lookups aren't case sensitive, e.g. "rd-1" still finds "RD-1"
    public static String normalise(String id) {
        return id == null ? "" : id.trim().toUpperCase();
    }
}
