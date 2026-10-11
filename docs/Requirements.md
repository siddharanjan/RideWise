# Requirements

## What it does

- Register riders and drivers
- List drivers that are currently free
- Request a ride - books a driver automatically using whichever matching strategy is configured
- Price the ride using whichever fare strategy is configured
- Complete a ride (generates a receipt)
- Track ride status: REQUESTED, ASSIGNED, COMPLETED, CANCELLED

A ride starts out REQUESTED. If a driver can be matched it becomes ASSIGNED, and completing it moves it to COMPLETED along with a FareReceipt. If nobody is free when the ride is requested, it gets marked CANCELLED instead of just disappearing - that's the only way CANCELLED actually shows up in this version since there's no "rider cancels mid-trip" menu option.

Other rules that are enforced on the entities themselves, not in the service layer:

- A driver can only be assigned to a REQUESTED ride.
- A ride can only be completed if it's ASSIGNED.
- A driver gets marked unavailable the moment they're assigned, and available again once the ride completes (their trip count goes up by one too). If the ride gets cancelled instead, they go back to available without the trip counting.
- Candidates for a ride are filtered by vehicle type before the matching strategy even sees them, so a bike request can't end up matched to a car driver.

## Non-functional

- Pricing and matching are both pluggable - see the strategy/ package. Adding a new one means adding a class, not editing RideService.
- RideService only depends on DriverService plus the two strategy interfaces. It doesn't touch RiderService directly - Main looks up the rider and passes the object in.
- No frameworks, no config files, no reflection. Everything is wired up by hand in Main's main() method.

## Fares

| Vehicle | Base fare | Per km |
|---------|-----------|--------|
| BIKE    | 15.0      | 6.0    |
| AUTO    | 25.0      | 9.0    |
| CAR     | 40.0      | 13.5   |

Default fare = base + (per km rate x distance). Peak hour strategy multiplies that by 1.5x between 8-11am and 5-9pm, otherwise it's the same as default.

## Running it

```bash
javac -d out $(find src -name "*.java")
java -cp out com.airtribe.ridewise.Main
java -cp out com.airtribe.ridewise.Main least-active
```

Ids are generated per type (RD-1, DR-1, RIDE-1), lookups aren't case sensitive.

## Example run

```
1  -> Asha, (0,0)              registers RD-1
2  -> Ravi, CAR, (3,4)          registers DR-1
2  -> Meera, CAR, (10,10)       registers DR-2
4  -> RD-1, CAR, 12.5 km        RIDE-1 assigned to Ravi (nearest, 5km away)
5  -> RIDE-1                    Receipt for RIDE-1: Rs. 208.75
6                                RIDE-1 | Asha | Ravi | 12.5 km | CAR | COMPLETED
```

## What's not in here

Payments, ratings, pooling multiple riders, real maps/GPS, persistence, login, concurrency. None of that was needed for the assignment and it would've just been more code to keep correct without adding to the design points being demonstrated. If any of it got added later, the places it'd plug in are noted in Class_Model.md.
