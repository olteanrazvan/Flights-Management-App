package ps.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ps.backend.dto.FlightDTO;
import ps.backend.model.Flight;
import ps.backend.repository.FlightRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlightService {

    private final FlightRepository flightRepository;

    @Autowired
    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional
    public FlightDTO createFlight(FlightDTO flightDTO) {
        // Check if flight number already exists
        if (flightRepository.findByFlightNumber(flightDTO.getFlightNumber()).isPresent()) {
            throw new RuntimeException("Flight number already exists");
        }

        // Create new flight from DTO
        Flight flight = new Flight(
                flightDTO.getFlightNumber(),
                flightDTO.getOrigin(),
                flightDTO.getDestination(),
                flightDTO.getDepartureTime(),
                flightDTO.getArrivalTime(),
                flightDTO.getTotalSeats(),
                flightDTO.getBasePrice()
        );

        // Save flight
        flight = flightRepository.save(flight);

        // Convert back to DTO and return
        return convertToDTO(flight);
    }

    /**
     * Update an existing flight
     *
     * @param id The ID of the flight to update
     * @param flightDTO DTO containing updated flight details
     * @return The updated flight
     */
    @Transactional
    public FlightDTO updateFlight(Long id, FlightDTO flightDTO) {
        // Find flight by ID
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        // Update flight details
        flight.setFlightNumber(flightDTO.getFlightNumber());
        flight.setOrigin(flightDTO.getOrigin());
        flight.setDestination(flightDTO.getDestination());
        flight.setDepartureTime(flightDTO.getDepartureTime());
        flight.setArrivalTime(flightDTO.getArrivalTime());
        flight.setTotalSeats(flightDTO.getTotalSeats());
        flight.setBasePrice(flightDTO.getBasePrice());

        // Don't update available seats directly, as it's managed by the system
        // based on ticket bookings

        // Save updated flight
        flight = flightRepository.save(flight);

        // Convert back to DTO and return
        return convertToDTO(flight);
    }

    /**
     * Get a flight by ID
     *
     * @param id The ID of the flight
     * @return The flight if found
     */
    public Optional<FlightDTO> getFlightById(Long id) {
        return flightRepository.findById(id)
                .map(this::convertToDTO);
    }

    /**
     * Get all flights
     *
     * @return List of all flights
     */
    public List<FlightDTO> getAllFlights() {
        return flightRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Search for flights based on criteria
     *
     * @param origin Origin city
     * @param destination Destination city
     * @param departureDate Departure date
     * @param passengers Number of passengers
     * @return List of flights matching the criteria
     */
    public List<FlightDTO> searchFlights(String origin, String destination, LocalDate departureDate, Integer passengers) {
        LocalDateTime startOfDay = departureDate.atStartOfDay();
        LocalDateTime endOfDay = departureDate.atTime(LocalTime.MAX);

        // Search for flights matching the criteria
        List<Flight> flights = flightRepository.findByOriginAndDestinationAndDepartureTimeBetween(
                origin,
                destination,
                startOfDay,
                endOfDay
        );

        // Filter by available seats if number of passengers is specified
        if (passengers != null && passengers > 0) {
            flights = flights.stream()
                    .filter(flight -> flight.getAvailableSeats() >= passengers)
                    .collect(Collectors.toList());
        }

        // Convert to DTOs and return
        return flights.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete a flight by ID
     *
     * @param id The ID of the flight to delete
     * @return true if deletion was successful, false otherwise
     */
    @Transactional
    public boolean deleteFlight(Long id) {
        if (flightRepository.existsById(id)) {
            flightRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Convert Flight entity to FlightDTO
     *
     * @param flight The Flight entity
     * @return FlightDTO
     */
    private FlightDTO convertToDTO(Flight flight) {
        FlightDTO dto = new FlightDTO();
        dto.setId(flight.getId());
        dto.setFlightNumber(flight.getFlightNumber());
        dto.setOrigin(flight.getOrigin());
        dto.setDestination(flight.getDestination());
        dto.setDepartureTime(flight.getDepartureTime());
        dto.setArrivalTime(flight.getArrivalTime());
        dto.setTotalSeats(flight.getTotalSeats());
        dto.setAvailableSeats(flight.getAvailableSeats());
        dto.setBasePrice(flight.getBasePrice());

        // Include ticket IDs if needed
        dto.setTicketIds(flight.getTickets().stream()
                .map(ticket -> ticket.getId())
                .collect(Collectors.toList()));

        return dto;
    }
}