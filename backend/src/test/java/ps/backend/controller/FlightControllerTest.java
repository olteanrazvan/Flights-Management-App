package ps.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ps.backend.dto.FlightDTO;
import ps.backend.service.FlightService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightControllerTest {

    @Mock
    private FlightService flightService;

    @InjectMocks
    private FlightController flightController;

    private FlightDTO testFlight;
    private List<FlightDTO> flights;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create test flight
        testFlight = new FlightDTO();
        testFlight.setId(1L);
        testFlight.setFlightNumber("FL123");
        testFlight.setOrigin("New York");
        testFlight.setDestination("London");
        testFlight.setDepartureTime(LocalDateTime.of(2025, 6, 1, 10, 0));
        testFlight.setArrivalTime(LocalDateTime.of(2025, 6, 1, 22, 0));
        testFlight.setTotalSeats(200);
        testFlight.setAvailableSeats(150);
        testFlight.setBasePrice(new BigDecimal("500.00"));

        // Create list of flights
        flights = new ArrayList<>();
        flights.add(testFlight);

        // Create a second flight
        FlightDTO flight2 = new FlightDTO();
        flight2.setId(2L);
        flight2.setFlightNumber("FL456");
        flight2.setOrigin("London");
        flight2.setDestination("Paris");
        flight2.setDepartureTime(LocalDateTime.of(2025, 6, 2, 14, 0));
        flight2.setArrivalTime(LocalDateTime.of(2025, 6, 2, 16, 0));
        flight2.setTotalSeats(150);
        flight2.setAvailableSeats(100);
        flight2.setBasePrice(new BigDecimal("250.00"));
        flights.add(flight2);
    }

    @Test
    void getAllFlights_shouldReturnAllFlights() {
        // Mock service to return all flights
        when(flightService.getAllFlights()).thenReturn(flights);

        // Call the controller method
        ResponseEntity<List<FlightDTO>> response = flightController.getAllFlights();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains(testFlight));

        // Verify the service was called correctly
        verify(flightService).getAllFlights();
    }

    @Test
    void getFlightById_shouldReturnFlight_whenFlightExists() {
        // Mock service to return the test flight
        when(flightService.getFlightById(testFlight.getId())).thenReturn(Optional.of(testFlight));

        // Call the controller method
        ResponseEntity<FlightDTO> response = flightController.getFlightById(testFlight.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testFlight.getId(), response.getBody().getId());
        assertEquals(testFlight.getFlightNumber(), response.getBody().getFlightNumber());

        // Verify the service was called correctly
        verify(flightService).getFlightById(testFlight.getId());
    }

    @Test
    void getFlightById_shouldReturnNotFound_whenFlightDoesNotExist() {
        // Mock service to return empty
        when(flightService.getFlightById(999L)).thenReturn(Optional.empty());

        // Call the controller method
        ResponseEntity<FlightDTO> response = flightController.getFlightById(999L);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        // Verify the service was called correctly
        verify(flightService).getFlightById(999L);
    }

    @Test
    void createFlight_shouldCreateFlight() {
        // Create new flight DTO for creation
        FlightDTO newFlight = new FlightDTO();
        newFlight.setFlightNumber("FL789");
        newFlight.setOrigin("Paris");
        newFlight.setDestination("Rome");
        newFlight.setDepartureTime(LocalDateTime.of(2025, 6, 3, 8, 0));
        newFlight.setArrivalTime(LocalDateTime.of(2025, 6, 3, 10, 0));
        newFlight.setTotalSeats(180);
        newFlight.setAvailableSeats(180);
        newFlight.setBasePrice(new BigDecimal("300.00"));

        // Set ID to simulate creation
        FlightDTO createdFlight = new FlightDTO();
        createdFlight.setId(3L);
        createdFlight.setFlightNumber(newFlight.getFlightNumber());
        createdFlight.setOrigin(newFlight.getOrigin());
        createdFlight.setDestination(newFlight.getDestination());
        createdFlight.setDepartureTime(newFlight.getDepartureTime());
        createdFlight.setArrivalTime(newFlight.getArrivalTime());
        createdFlight.setTotalSeats(newFlight.getTotalSeats());
        createdFlight.setAvailableSeats(newFlight.getAvailableSeats());
        createdFlight.setBasePrice(newFlight.getBasePrice());

        // Mock service to return the created flight
        when(flightService.createFlight(newFlight)).thenReturn(createdFlight);

        // Call the controller method
        ResponseEntity<FlightDTO> response = flightController.createFlight(newFlight);

        // Verify the response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdFlight.getId(), response.getBody().getId());
        assertEquals(newFlight.getFlightNumber(), response.getBody().getFlightNumber());

        // Verify the service was called correctly
        verify(flightService).createFlight(newFlight);
    }

    @Test
    void updateFlight_shouldUpdateFlight() {
        // Create updated flight DTO
        FlightDTO updatedFlight = new FlightDTO();
        updatedFlight.setFlightNumber(testFlight.getFlightNumber());
        updatedFlight.setOrigin(testFlight.getOrigin());
        updatedFlight.setDestination("Paris"); // Changed destination
        updatedFlight.setDepartureTime(testFlight.getDepartureTime());
        updatedFlight.setArrivalTime(LocalDateTime.of(2025, 6, 1, 20, 0)); // Earlier arrival
        updatedFlight.setTotalSeats(testFlight.getTotalSeats());
        updatedFlight.setAvailableSeats(testFlight.getAvailableSeats());
        updatedFlight.setBasePrice(new BigDecimal("550.00")); // Increased price

        // Mock service to return the updated flight
        when(flightService.updateFlight(testFlight.getId(), updatedFlight)).thenReturn(updatedFlight);

        // Call the controller method
        ResponseEntity<FlightDTO> response = flightController.updateFlight(testFlight.getId(), updatedFlight);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Paris", response.getBody().getDestination());
        assertEquals(LocalDateTime.of(2025, 6, 1, 20, 0), response.getBody().getArrivalTime());
        assertEquals(new BigDecimal("550.00"), response.getBody().getBasePrice());

        // Verify the service was called correctly
        verify(flightService).updateFlight(testFlight.getId(), updatedFlight);
    }

    @Test
    void deleteFlight_shouldDeleteFlight_whenFlightExists() {
        // Mock service to return successful deletion
        when(flightService.deleteFlight(testFlight.getId())).thenReturn(true);

        // Call the controller method
        ResponseEntity<Void> response = flightController.deleteFlight(testFlight.getId());

        // Verify the response
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify the service was called correctly
        verify(flightService).deleteFlight(testFlight.getId());
    }

    @Test
    void deleteFlight_shouldReturnNotFound_whenFlightDoesNotExist() {
        // Mock service to return failed deletion
        when(flightService.deleteFlight(999L)).thenReturn(false);

        // Call the controller method
        ResponseEntity<Void> response = flightController.deleteFlight(999L);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        // Verify the service was called correctly
        verify(flightService).deleteFlight(999L);
    }

    @Test
    void searchFlights_shouldReturnMatchingFlights() {
        String origin = "New York";
        String destination = "London";
        String departureDateStr = "2025-06-01";
        LocalDate departureDate = LocalDate.parse(departureDateStr);
        Integer passengers = 2;

        // Create search result
        List<FlightDTO> searchResults = new ArrayList<>();
        searchResults.add(testFlight);

        // Mock service to return search results
        when(flightService.searchFlights(origin, destination, departureDate, passengers)).thenReturn(searchResults);

        // Call the controller method
        ResponseEntity<List<FlightDTO>> response = flightController.searchFlights(origin, destination, departureDateStr, passengers);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testFlight.getFlightNumber(), response.getBody().get(0).getFlightNumber());

        // Verify the service was called correctly
        verify(flightService).searchFlights(origin, destination, departureDate, passengers);
    }

    @Test
    void searchFlights_shouldReturnEmptyList_whenNoMatchesFound() {
        String origin = "Rome";
        String destination = "Tokyo";
        String departureDateStr = "2025-07-01";
        LocalDate departureDate = LocalDate.parse(departureDateStr);
        Integer passengers = 5;

        // Mock service to return empty list
        when(flightService.searchFlights(origin, destination, departureDate, passengers)).thenReturn(new ArrayList<>());

        // Call the controller method
        ResponseEntity<List<FlightDTO>> response = flightController.searchFlights(origin, destination, departureDateStr, passengers);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        // Verify the service was called correctly
        verify(flightService).searchFlights(origin, destination, departureDate, passengers);
    }
}