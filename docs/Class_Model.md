# Class Model

## Rough layering

```
Main (console only)
  |
  v
service/  RiderService   DriverService   RideService --> strategy/ interfaces
  |                           |                |
  v                           v                v
model/   Rider  Driver  Ride  FareReceipt  Location  RideStatus  VehicleType
```

Dependencies only go one direction - Main depends on services, services depend on model + strategy interfaces. Nothing in model/ knows services exist, nothing in strategy/ knows the console exists.

## Packages

- `model` - entities + enums, and the rules that keep their state valid
- `strategy` - the two interfaces (RideMatchingStrategy, FareStrategy) and their implementations
- `service` - RiderService, DriverService, RideService
- `exception` - NoDriverAvailableException
- `util` - IdGenerator

## Entities

**Location** - just (x, y), immutable, has distanceTo(). Mainly exists because NearestDriverStrategy needs actual distance math and that logic shouldn't be duplicated anywhere.

**Rider** - id, name, location. moveTo() is the only way to change the location.

**Driver** - id, name, vehicleType, currentLocation, available, completedTrips. State changes go through goOnTrip()/finishTrip()/abortTrip() instead of a plain setter for `available`, because finishTrip() also bumps the trip counter and those two things need to always happen together. finishTrip() throws if called on a driver that isn't actually on a trip.

**Ride** - id, rider, driver, vehicleType, distanceInKm, status, requestedAt, receipt. Enforces its own state machine - assignTo() only works from REQUESTED, complete() only from ASSIGNED, cancel() refuses if the ride already finished one way or another. getDriver() and getReceipt() return Optional since "no driver yet" / "not priced yet" are both normal states, not errors.

**FareReceipt** - immutable, rideId + amount + timestamp. Rejects a negative amount. Only Ride.complete() ever creates one.

**RideStatus** - the 4 states from the lifecycle.

**VehicleType** - carries baseFare and ratePerKm as enum fields, since those numbers are a property of the vehicle itself, not something a pricing strategy should be hardcoding.

## Services

**RiderService** - registerRider, getRider, getAllRiders. The only place a Rider gets constructed, which keeps the ids from colliding.

**DriverService** - registerDriver, getDriver, updateAvailability, getAvailableDrivers() (both overloads), getAllDrivers, bookForTrip, releaseAfterTrip, releaseWithoutTrip. Both getAvailableDrivers overloads go through the same private findDrivers(Predicate) helper so there's one definition of "who's free" instead of two slightly different ones.

**RideService** - requestRide, completeRide, cancelRide, getRide, getAllRides. Constructor takes (DriverService, RideMatchingStrategy, FareStrategy). It's mostly sequencing: grab candidates, ask the strategy who takes it, tell the ride, tell the driver pool. Same idea on completion - ask the fare strategy for a price, tell the ride to close, free up the driver.

## Strategies

```java
public interface RideMatchingStrategy {
    Driver findDriver(Rider rider, List<Driver> drivers);  // null if nobody can take it
}

public interface FareStrategy {
    double calculateFare(Ride ride);
}
```

- NearestDriverStrategy - smallest distance to the rider, ties broken by driver id
- LeastActiveDriverStrategy - fewest completed trips, same tie-break
- DefaultFareStrategy - base + per km
- PeakHourFareStrategy - wraps another FareStrategy and multiplies during rush hour. Takes a Clock as a constructor param so the peak-hour logic can be tested without actually running the test at 6pm.

## Where new features would plug in

- New pricing rule (surge by demand, discounts, etc.) -> new FareStrategy
- New matching logic (driver ratings, ETA, preferences) -> new RideMatchingStrategy
- Driver ratings -> a field on Driver plus a strategy that reads it
- Persistence -> swap the Maps inside the services for repository classes
- A web/REST frontend instead of console -> replace Main, services don't change
