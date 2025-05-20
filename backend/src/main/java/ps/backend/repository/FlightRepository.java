package ps.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ps.backend.model.Flight;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    List<Flight> findByOriginAndDestination(String origin, String destination);

    List<Flight> findByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Flight> findByOriginAndDestinationAndDepartureTimeBetween(
            String origin,
            String destination,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Flight> findByAvailableSeatsGreaterThan(Integer seats);
}