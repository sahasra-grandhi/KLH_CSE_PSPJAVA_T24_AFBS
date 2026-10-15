import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

class FlightException extends Exception {
    public FlightException(String message) {
        super(message);
    }
}

class Flight implements Comparable<Flight> {
    private String flightNumber;
    private String destination;
    private LocalTime departureTime;
    private String status;
    private int gate;

    public Flight(String flightNumber, String destination, LocalTime departureTime, String status, int gate) {
        this.flightNumber = flightNumber;
        this.destination = destination;
        this.departureTime = departureTime;
        this.status = status;
        this.gate = gate;
    }

    public String getFlightNumber() { return flightNumber; }
    public String getDestination() { return destination; }
    public LocalTime getDepartureTime() { return departureTime; }
    public String getStatus() { return status; }
    public int getGate() { return gate; }
    @Override
    public int compareTo(Flight other) {
        return this.departureTime.compareTo(other.departureTime);
    }

    @Override
    public String toString() {
        return "Flight: " + flightNumber + " | Dest: " + destination + " | Time: " + departureTime + " | Gate: " + gate + " | Status: " + status;
    }

    public String toFileString() {
        return flightNumber + "," + destination + "," + departureTime + "," + status + "," + gate;
    }

    public static Flight fromFileString(String line) throws FlightException {
        
        String[] parts = line.split(",");
        if (parts.length != 5) {
            throw new FlightException("Malformed record");
        }
        return new Flight(parts[0].trim(), parts[1].trim(), LocalTime.parse(parts[2].trim()), parts[3].trim(), Integer.parseInt(parts[4].trim()));
    }
}

class AirportScheduler {
    
    private final List<Flight> flightList = new ArrayList<>();
    private final Map<String, Flight> flightMap = new HashMap<>();
    private final String fileName = "flights_database.txt";

    public void addFlight(Flight flight) throws FlightException {
        if (flightMap.containsKey(flight.getFlightNumber().toUpperCase())) {
            throw new FlightException("Flight already exists!");
        }
        flightList.add(flight);
        flightMap.put(flight.getFlightNumber().toUpperCase(), flight);
        Collections.sort(flightList);
    }

    public void removeFlight(String flightNumber) throws FlightException {
        Flight flight = flightMap.remove(flightNumber.toUpperCase());
        if (flight == null) {
            throw new FlightException("Flight not found.");
        }
        flightList.remove(flight);
    }

    public void displayBoard() {
        flightList.forEach(System.out::println);
    }

    public void searchByDestination(String destination) {
        List<Flight> results = flightList.stream()
                .filter(f -> f.getDestination().equalsIgnoreCase(destination))
                .collect(Collectors.toList());
        results.forEach(System.out::println);
    }

    public void saveToFile() {
        Path path = Paths.get(fileName);
        List<String> lines = flightList.stream().map(Flight::toFileString).collect(Collectors.toList());
        try {
            Files.write(path, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error saving file.");
        }
    }

    public void loadFromFile() {
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) return;
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                try {
                    Flight f = Flight.fromFileString(line);
                    flightList.add(f);
                    flightMap.put(f.getFlightNumber().toUpperCase(), f);
                } catch (Exception ignored) {}
            }
            Collections.sort(flightList);
        } catch (IOException ignored) {}
    }
}

public class AirportFlightBoardScheduler {
    public static void main(String[] args) {
        AirportScheduler scheduler = new AirportScheduler();
        scheduler.loadFromFile();
        
        Scanner scanner = new Scanner(System.in);
        int choice = 0;

        do {
            System.out.println("1. View Board | 2. Add Flight | 3. Remove | 4. Search | 5. Save & Exit");
            System.out.print("Choice: ");

            try {
                choice = Integer.parseInt(scanner.nextLine());

                switch (choice) {
                    case 1:
                        scheduler.displayBoard();
                        break;
                    case 2:
                        System.out.print("Flight No: ");
                        String fNo = scanner.nextLine();
                        System.out.print("Destination: ");
                        String dest = scanner.nextLine();
                        System.out.print("Time (HH:MM): ");
                        LocalTime time = LocalTime.parse(scanner.nextLine());
                        System.out.print("Status: ");
                        String status = scanner.nextLine();
                        System.out.print("Gate: ");
                        int gate = Integer.parseInt(scanner.nextLine());

                        scheduler.addFlight(new Flight(fNo, dest, time, status, gate));
                        break;
                    case 3:
                        System.out.print("Remove Flight No: ");
                        scheduler.removeFlight(scanner.nextLine());
                        break;
                    case 4:
                        System.out.print("Destination search: ");
                        scheduler.searchByDestination(scanner.nextLine());
                        break;
                    case 5:
                        scheduler.saveToFile();
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException | DateTimeParseException e) {
                System.out.println("Format error.");
            } catch (FlightException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (choice != 5); 

        scanner.close();
    }
}