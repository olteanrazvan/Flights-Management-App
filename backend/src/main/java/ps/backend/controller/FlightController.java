package ps.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ps.backend.dto.FlightDTO;
import ps.backend.service.FlightService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    @Autowired
    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    /**
     * Get all flights
     *
     * @return List of all flights
     */
    @GetMapping
    public ResponseEntity<List<FlightDTO>> getAllFlights() {
        List<FlightDTO> flights = flightService.getAllFlights();
        return ResponseEntity.ok(flights);
    }

    /**
     * Get a flight by ID
     *
     * @param id Flight ID
     * @return The flight if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getFlightById(@PathVariable Long id) {
        Optional<FlightDTO> flightOpt = flightService.getFlightById(id);

        return flightOpt
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new flight (admin only)
     *
     * @param flightDTO DTO containing flight details
     * @return The created flight
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlightDTO> createFlight(@RequestBody FlightDTO flightDTO) {
        FlightDTO createdFlight = flightService.createFlight(flightDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFlight);
    }

    /**
     * Update an existing flight (admin only)
     *
     * @param id Flight ID
     * @param flightDTO DTO containing updated flight details
     * @return The updated flight
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlightDTO> updateFlight(@PathVariable Long id, @RequestBody FlightDTO flightDTO) {
        FlightDTO updatedFlight = flightService.updateFlight(id, flightDTO);
        return ResponseEntity.ok(updatedFlight);
    }

    /**
     * Delete a flight (admin only)
     *
     * @param id Flight ID
     * @return No content if successful
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        boolean deleted = flightService.deleteFlight(id);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
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
    @GetMapping("/search")
    public ResponseEntity<List<FlightDTO>> searchFlights(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam String departureDate,
            @RequestParam(required = false) Integer passengers) {

        LocalDate parsedDate = LocalDate.parse(departureDate);
        List<FlightDTO> flights = flightService.searchFlights(origin, destination, parsedDate, passengers);
        return ResponseEntity.ok(flights);
    }
}