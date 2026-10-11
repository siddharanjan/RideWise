# RideWise

Console-based ride sharing app (like a tiny Uber/Ola) built for an LLD practice assignment. The feature set is intentionally small - the point was to get the OOP/SOLID design right, not to build something feature-complete.

## Build & run

```bash
javac -d out $(find src -name "*.java")
java -cp out com.airtribe.ridewise.Main
```

There's a second driver-matching strategy you can switch to from the command line:

```bash
java -cp out com.airtribe.ridewise.Main least-active
```

Needs JDK 11+, no external libraries.

## Menu

```
1. Add Rider
2. Add Driver
3. View Available Drivers
4. Request Ride
5. Complete Ride
6. View Rides
7. Exit
```

Ids look like RD-1 (rider), DR-1 (driver), RIDE-1 (ride). Locations are just x,y points on a grid - "nearest driver" is straight-line distance, nothing like real road routing.

## Package layout

```
src/com/airtribe/ridewise/
  Main.java      console menu
  model/         Rider, Driver, Ride, FareReceipt, Location, RideStatus, VehicleType
  strategy/      RideMatchingStrategy (+2 impls), FareStrategy (+2 impls)
  service/       RiderService, DriverService, RideService
  exception/     NoDriverAvailableException
  util/          IdGenerator
docs/            notes on requirements, class design, relationships, SOLID
```

More on the design decisions is in the `docs/` folder if you want it.
