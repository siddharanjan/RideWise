# Object Relationships

| From | To | Relationship | Notes |
|------|----|--------------|-------|
| Rider | Ride | association | Ride holds a Rider reference, rider exists independently |
| Driver | Ride | association | same idea, driver outlives the ride |
| Ride | FareReceipt | composition | only Ride.complete() creates one, dies with the ride |
| RideService | RideMatchingStrategy | composition | constructor-injected, held final |
| RideService | FareStrategy | composition | constructor-injected, held final |
| RideService | DriverService | aggregation | passed in, shared with Main, not owned |
| Rider / Driver | Location | composition (value object) | immutable, replaced not mutated |
| Driver | VehicleType | association | enum constant shared across drivers |

## Ride -> FareReceipt

This is composition because a receipt means nothing without the ride it belongs to. It's created inside `Ride.complete(double)`:

```java
public FareReceipt complete(double fare) {
    requireStatus(RideStatus.ASSIGNED, "complete the ride");
    this.receipt = new FareReceipt(id, fare, LocalDateTime.now());
    this.status = RideStatus.COMPLETED;
    return receipt;
}
```

Since the receipt gets built in the same method that flips the status to COMPLETED, there's no way for a ride to end up completed without a receipt or vice versa. If a service built the receipt separately, those two things could drift out of sync.

## Rider/Driver -> Ride

Riders and drivers exist before and after any particular ride, so a ride just holds a reference to them rather than owning them. Deleting a ride shouldn't delete the rider.

## Why strategies are composed instead of subclassed

Could've written `PeakHourFare extends DefaultFare`, but:

1. Peak pricing and base pricing are really two separate decisions. Wrapping means PeakHourFareStrategy works on top of any base rule, including ones added later, without a new subclass for every combination.
2. Subclassing ties the subclass to the parent's internals. Wrapping only depends on the interface.

```java
FareStrategy fareStrategy = new PeakHourFareStrategy(new DefaultFareStrategy(), 1.5);
```

Same logic applies to matching - RideService *has* a strategy rather than being subclassed per strategy, so there's no NearestRideService / LeastActiveRideService pair to keep in sync with each other.

## Law of Demeter

- NearestDriverStrategy calls `driver.distanceFrom(rider.getLocation())` instead of `driver.getCurrentLocation().distanceTo(rider.getLocation())`. The driver answers for its own position.
- DefaultFareStrategy reads `ride.getVehicleType().getBaseFare()` - the rate numbers live on the enum, not reached for through a driver reference (that would've been ride -> driver -> vehicleType -> rate, a 3-hop chain, which is part of why Ride stores its own vehicleType instead of relying on the assigned driver's).
- RideService.completeRide pulls the driver into a local variable and hands the whole object to driverService.releaseAfterTrip(driver) - it doesn't reach through the ride to flip a flag on the driver directly.
- Main asks services for things and prints them, it doesn't chain through one entity to get to another.

## Rough object lifetime

```
registerRider  -> Rider stored in RiderService's map
registerDriver -> Driver stored in DriverService's map
requestRide    -> Ride created (REQUESTED) -> strategy picks a Driver
                  -> assignTo (ASSIGNED) -> driver booked
                  -> or no candidate: cancel (CANCELLED)
completeRide   -> fare strategy prices it -> complete() builds the receipt (COMPLETED)
                  -> driver released, trip count +1
```
