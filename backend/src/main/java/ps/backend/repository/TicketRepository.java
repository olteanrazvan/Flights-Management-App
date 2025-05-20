package ps.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ps.backend.model.Ticket;
import ps.backend.model.User;
import ps.backend.model.Flight;
import ps.backend.model.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByUser(User user);

    List<Ticket> findByFlight(Flight flight);

    List<Ticket> findByUserAndStatus(User user, TicketStatus status);

    List<Ticket> findByFlightAndStatus(Flight flight, TicketStatus status);

    List<Ticket> findByPurchaseTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Ticket> findByPassengerEmail(String email);
}