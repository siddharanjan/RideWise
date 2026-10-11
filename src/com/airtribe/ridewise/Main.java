package com.airtribe.ridewise;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.FareReceipt;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.service.DriverService;
import com.airtribe.ridewise.service.RideService;
import com.airtribe.ridewise.service.RiderService;
import com.airtribe.ridewise.strategy.DefaultFareStrategy;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.LeastActiveDriverStrategy;
import com.airtribe.ridewise.strategy.NearestDriverStrategy;
import com.airtribe.ridewise.strategy.PeakHourFareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

// console menu for the app. run with "least-active" as an arg to switch
// the driver matching strategy, e.g.:
//   java com.airtribe.ridewise.Main least-active
public class Main {

    private static final double SURGE_MULTIPLIER = 1.5;

    private final Scanner input;
    private final RiderService riderService;
    private final DriverService driverService;
    private final RideService rideService;

    Main(Scanner input, RiderService riderService, DriverService driverService, RideService rideService) {
        this.input = input;
        this.riderService = riderService;
        this.driverService = driverService;
        this.rideService = rideService;
    }

    public static void main(String[] args) {
        RideMatchingStrategy matchingStrategy = matchingStrategyFrom(args);
        FareStrategy fareStrategy = new PeakHourFareStrategy(new DefaultFareStrategy(), SURGE_MULTIPLIER);

        DriverService driverService = new DriverService();
        RideService rideService = new RideService(driverService, matchingStrategy, fareStrategy);

        System.out.println("=== RideWise ===");
        System.out.println("Matching rule : " + matchingStrategy);
        System.out.println("Pricing rule  : " + fareStrategy);

        try (Scanner scanner = new Scanner(System.in)) {
            new Main(scanner, new RiderService(), driverService, rideService).run();
        }
    }

    private static RideMatchingStrategy matchingStrategyFrom(String[] args) {
        boolean leastActive = Arrays.stream(args).anyMatch("least-active"::equalsIgnoreCase);
        return leastActive ? new LeastActiveDriverStrategy() : new NearestDriverStrategy();
    }

    private void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = readLine("Choose an option: ");
            if (choice == null) {
                return;
            }
            try {
                running = handle(choice.trim());
            } catch (NoDriverAvailableException e) {
                System.out.println("Sorry: " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Could not do that: " + e.getMessage());
            }
        }
        System.out.println("Thanks for using RideWise.");
    }

    private boolean handle(String choice) {
        switch (choice) {
            case "1":
                addRider();
                return true;
            case "2":
                addDriver();
                return true;
            case "3":
                showAvailableDrivers();
                return true;
            case "4":
                requestRide();
                return true;
            case "5":
                completeRide();
                return true;
            case "6":
                showRides();
                return true;
            case "7":
                return false;
            default:
                System.out.println("Pick a number between 1 and 7.");
                return true;
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Add Rider");
        System.out.println("2. Add Driver");
        System.out.println("3. View Available Drivers");
        System.out.println("4. Request Ride");
        System.out.println("5. Complete Ride");
        System.out.println("6. View Rides");
        System.out.println("7. Exit");
    }

    private void addRider() {
        String name = readLine("Rider name: ");
        Location location = readLocation("Rider");
        Rider rider = riderService.registerRider(name, location);
        System.out.printf("Registered %s (%s) at %s%n", rider.getName(), rider.getId(), rider.getLocation());
    }

    private void addDriver() {
        String name = readLine("Driver name: ");
        VehicleType vehicleType = readVehicleType();
        Location location = readLocation("Driver");
        Driver driver = driverService.registerDriver(name, vehicleType, location);
        System.out.printf("Registered %s (%s) driving a %s%n",
                driver.getName(), driver.getId(), driver.getVehicleType());
    }

    private void showAvailableDrivers() {
        List<Driver> drivers = driverService.getAvailableDrivers();
        if (drivers.isEmpty()) {
            System.out.println("No drivers are available at the moment.");
            return;
        }
        System.out.println("Available drivers:");
        for (Driver driver : drivers) {
            System.out.println("  " + driver);
        }
    }

    private void requestRide() {
        Rider rider = riderService.getRider(readLine("Rider id: "));
        VehicleType vehicleType = readVehicleType();
        double distance = readDouble("Trip distance in km: ");

        Ride ride = rideService.requestRide(rider, vehicleType, distance);
        Driver driver = ride.getDriver().orElseThrow();
        System.out.printf("Ride %s confirmed - %s (%s) is on the way, %.1f km away%n",
                ride.getId(), driver.getName(), driver.getId(), driver.distanceFrom(rider.getLocation()));
    }

    private void completeRide() {
        FareReceipt receipt = rideService.completeRide(readLine("Ride id: "));
        System.out.println(receipt);
    }

    private void showRides() {
        Collection<Ride> rides = rideService.getAllRides();
        if (rides.isEmpty()) {
            System.out.println("No rides booked yet.");
            return;
        }
        System.out.println("Rides so far:");
        for (Ride ride : rides) {
            System.out.println("  " + ride + fareSuffix(ride));
        }
    }

    private String fareSuffix(Ride ride) {
        return ride.getReceipt()
                .map(receipt -> String.format(" | Rs. %.2f", receipt.getAmount()))
                .orElseGet(() -> ride.getCancellationReason().map(reason -> " | " + reason).orElse(""));
    }

    private Location readLocation(String owner) {
        double x = readDouble(owner + " location X: ");
        double y = readDouble(owner + " location Y: ");
        return new Location(x, y);
    }

    private VehicleType readVehicleType() {
        while (true) {
            System.out.println("Vehicle types: " + Arrays.toString(VehicleType.values()));
            String value = readLine("Vehicle type: ");
            try {
                return VehicleType.valueOf(requireInput(value).trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("That is not a vehicle type we run. Try again.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Double.parseDouble(requireInput(value).trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number, for example 4.5");
            }
        }
    }

    // returns null instead of throwing when input runs out, so the menu
    // loop can just exit cleanly
    private String readLine(String prompt) {
        System.out.print(prompt);
        return input.hasNextLine() ? input.nextLine() : null;
    }

    private String requireInput(String value) {
        if (value == null) {
            throw new IllegalStateException("Input ended before the entry was complete");
        }
        return value;
    }
}
