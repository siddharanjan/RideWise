# SOLID / Design Principles - notes

## Single Responsibility

RiderService only deals with rider registration. DriverService only deals with the driver pool. RideService only deals with the ride lifecycle (and asks the two strategy interfaces to actually do the matching/pricing work). Ride owns its own state transitions but doesn't know *how* it got priced, just what the number was.

My first draft had the peak-hour check as an `if` statement directly inside RideService.calculateFare(). It worked fine, but it meant that one method had two reasons to change (pricing logic and ride lifecycle stuff), so I pulled it out into its own strategy class.

## Open/Closed

Adding a new fare rule or matching rule is just adding a new class - nothing existing needs touching. A weekend-discount rule, for example, would look like:

```java
public class WeekendFareStrategy implements FareStrategy {
    private final FareStrategy base;
    // multiply by something on Sat/Sun
}
```

and then a one-line change in Main to use it instead of DefaultFareStrategy. RideService, Ride, and the other strategies don't change. I added LeastActiveDriverStrategy after NearestDriverStrategy was already working and didn't have to touch RideService for that either, which is basically proof the interface boundary is in the right place.

## Liskov Substitution

Both matching strategies filter by `isAvailable()` the same way and return null the same way when nobody's free, so RideService treats either one identically. Same for FareStrategy - any implementation reads the ride and returns a number, doesn't touch the ride or build a receipt. PeakHourFareStrategy is a decent test of this since it wraps a FareStrategy without caring which one, and it's itself usable anywhere a FareStrategy is expected (you could even stack it on top of another wrapped strategy if you wanted more rules layered on).

## Interface Segregation

Both strategy interfaces have exactly one method. Pricing and matching aren't lumped into one big "RideStrategy" interface, because then a pricing implementation would need some dummy findDriver() method it never actually uses.

## Dependency Inversion

RideService's constructor takes the two interfaces, not concrete classes:

```java
public RideService(DriverService driverService,
                   RideMatchingStrategy matchingStrategy,
                   FareStrategy fareStrategy) { ... }
```

Only Main actually names NearestDriverStrategy / DefaultFareStrategy etc - everything downstream just uses the interface. One thing I went back and forth on: DriverService is passed in as a concrete class, not behind an interface. Left it that way on purpose - it's a same-layer collaborator with one implementation, and a DriverPool interface for it right now would be ceremony without a real benefit. If persistence gets added later, that's probably when splitting it out would actually pay off.

## DRY

- Driver filtering happens in one place - DriverService.findDrivers(Predicate). Both getAvailableDrivers() overloads go through it.
- Fare rate numbers live once, on VehicleType, not copy-pasted into each strategy.
- Receipt creation happens once, inside Ride.complete().
- Case-insensitive id lookup (trim + uppercase) is one static method on IdGenerator instead of being rewritten in all three services.

## KISS

Locations are just grid points, not real addresses or geocoding. Ids are readable strings (RD-1, DR-1) instead of UUIDs. Storage is plain LinkedHashMaps in each service - no DB, no builders, no factory classes for 3-field entities. Felt like the right amount of complexity for what this is.

## YAGNI

Didn't build: payments, ratings, ride pooling, driver shifts/scheduling, async anything, persistence. All of those are easy to imagine adding, but none of them demonstrate anything new about the design, they'd just be more surface area. cancelRide() does exist, but mainly because "no driver available" needed somewhere for the ride to end up - not because there's an actual cancel button in the menu.

The one thing I kept that wasn't strictly required by any menu item: PeakHourFareStrategy takes a Clock instead of just calling LocalTime.now() directly. Without that there was no way to test the peak/off-peak branching without waiting around for the right time of day.

## Law of Demeter

More examples in Object_Relationships.md. Short version - nothing chains more than one hop. Ride stores its own requested VehicleType so fare strategies don't need to go ride -> driver -> vehicle -> rate. RideService hands whole Driver objects over to DriverService rather than poking at a flag through the ride.

## If this had to scale up for real

- The maps inside the services would need to become actual repositories/DB-backed storage instead of plain HashMaps.
- RideMatchingStrategy returning a single Driver (or null) is a bit limiting. Real dispatch would probably want to try multiple drivers in order if the first one declines, and Optional<Driver> instead of a nullable return would be cleaner on the interface.
- double for money is asking for rounding bugs eventually - BigDecimal would be safer.
- Nothing here is thread-safe, which is fine for a single-user console app but wouldn't survive two riders hitting it at once in a real system.
