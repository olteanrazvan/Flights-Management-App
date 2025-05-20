package ps.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ps.backend.dto.TicketDTO;
import ps.backend.model.Flight;
import ps.backend.model.Ticket;
import ps.backend.model.TicketStatus;
import ps.backend.model.User;
import ps.backend.observer.TicketEventType;
import ps.backend.observer.TicketObserver;
import ps.backend.observer.TicketSubject;
import ps.backend.repository.FlightRepository;
import ps.backend.repository.TicketRepository;
import ps.backend.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService implements TicketSubject {

    private final TicketRepository ticketRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final List<TicketObserver> observers = new ArrayList<>();

    @Autowired
    public TicketService(
            TicketRepository ticketRepository,
            FlightRepository flightRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.flightRepository = flightRepository;
        this.userRepository = userRepository;

        // Register the notification service as an observer
        registerObserver(notificationService);
    }

    @Override
    public void registerObserver(TicketObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(TicketObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Ticket ticket, TicketEventType eventType) {
        for (TicketObserver observer : observers) {
            observer.update(ticket, eventType);
        }
    }

    /**
     * Create a new ticket for a flight
     *
     * @param ticketDTO DTO containing ticket information
     * @return The created ticket DTO
     */
    @Transactional
    public TicketDTO createTicket(TicketDTO ticketDTO) {
        Flight flight = flightRepository.findById(ticketDTO.getFlightId())
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        if (!flight.hasAvailableSeats()) {
            throw new RuntimeException("No available seats for this flight");
        }

        User user = userRepository.findById(ticketDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Calculate ticket price (could include more complex logic)
        BigDecimal ticketPrice = ticketDTO.getPrice() != null ?
                ticketDTO.getPrice() : flight.getBasePrice();

        // Create the ticket
        Ticket ticket = new Ticket(
                flight,
                user,
                ticketDTO.getPassengerName(),
                ticketDTO.getPassengerEmail(),
                ticketPrice,
                ticketDTO.getSeatNumber()
        );

        // Update flight's available seats
        flight.decrementAvailableSeats();
        flightRepository.save(flight);

        // Save the ticket
        ticket = ticketRepository.save(ticket);

        // Notify observers about the ticket creation
        notifyObservers(ticket, TicketEventType.CREATED);

        return convertToDTO(ticket);
    }

    /**
     * Confirm a ticket
     *
     * @param ticketId The ID of the ticket to confirm
     * @return The confirmed ticket DTO
     */
    @Transactional
    public TicketDTO confirmTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.confirmTicket();
        ticket = ticketRepository.save(ticket);

        // Notify observers about the ticket confirmation
        notifyObservers(ticket, TicketEventType.CONFIRMED);

        return convertToDTO(ticket);
    }

    /**
     * Cancel a ticket
     *
     * @param ticketId The ID of the ticket to cancel
     * @return The cancelled ticket DTO
     */
    @Transactional
    public TicketDTO cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.cancelTicket();

        // The cancelTicket method already increments flight's available seats
        flightRepository.save(ticket.getFlight());
        ticket = ticketRepository.save(ticket);

        // Notify observers about the ticket cancellation
        notifyObservers(ticket, TicketEventType.CANCELLED);

        return convertToDTO(ticket);
    }

    /**
     * Update a ticket
     *
     * @param ticketId The ID of the ticket to update
     * @param ticketDTO DTO containing updated ticket information
     * @return The updated ticket DTO
     */
    @Transactional
    public TicketDTO updateTicket(Long ticketId, TicketDTO ticketDTO) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        // Update mutable fields
        if (ticketDTO.getPassengerName() != null) {
            ticket.setPassengerName(ticketDTO.getPassengerName());
        }

        if (ticketDTO.getPassengerEmail() != null) {
            ticket.setPassengerEmail(ticketDTO.getPassengerEmail());
        }

        if (ticketDTO.getSeatNumber() != null) {
            ticket.setSeatNumber(ticketDTO.getSeatNumber());
        }

        // Save updated ticket
        ticket = ticketRepository.save(ticket);

        // Notify observers about the ticket update
        notifyObservers(ticket, TicketEventType.UPDATED);

        return convertToDTO(ticket);
    }

    /**
     * Get a ticket by ID
     *
     * @param ticketId The ID of the ticket to retrieve
     * @return The ticket DTO if found
     */
    public Optional<TicketDTO> getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .map(this::convertToDTO);
    }

    /**
     * Get a ticket by ticket number
     *
     * @param ticketNumber The ticket number to search for
     * @return The ticket DTO if found
     */
    public Optional<TicketDTO> getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .map(this::convertToDTO);
    }

    /**
     * Get all tickets for a user
     *
     * @param userId The ID of the user
     * @return List of ticket DTOs for the user
     */
    public List<TicketDTO> getTicketsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ticketRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all tickets for a flight
     *
     * @param flightId The ID of the flight
     * @return List of ticket DTOs for the flight
     */
    public List<TicketDTO> getTicketsByFlight(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));

        return ticketRepository.findByFlight(flight).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tickets by status
     *
     * @param userId The ID of the user
     * @param status The ticket status
     * @return List of ticket DTOs with the specified status
     */
    public List<TicketDTO> getTicketsByStatus(Long userId, TicketStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ticketRepository.findByUserAndStatus(user, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get tickets purchased between specific dates
     *
     * @param start Start date and time
     * @param end End date and time
     * @return List of ticket DTOs purchased in the specified time range
     */
    public List<TicketDTO> getTicketsByPurchaseDate(LocalDateTime start, LocalDateTime end) {
        return ticketRepository.findByPurchaseTimeBetween(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Ticket entity to TicketDTO
     *
     * @param ticket Ticket entity
     * @return TicketDTO
     */
    private TicketDTO convertToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setTicketNumber(ticket.getTicketNumber());
        dto.setFlightId(ticket.getFlight().getId());
        dto.setUserId(ticket.getUser().getId());
        dto.setPassengerName(ticket.getPassengerName());
        dto.setPassengerEmail(ticket.getPassengerEmail());
        dto.setPrice(ticket.getPrice());
        dto.setPurchaseTime(ticket.getPurchaseTime());
        dto.setSeatNumber(ticket.getSeatNumber());
        dto.setStatus(ticket.getStatus());

        // Include flight details for convenience
        dto.setFlightNumber(ticket.getFlight().getFlightNumber());
        dto.setOrigin(ticket.getFlight().getOrigin());
        dto.setDestination(ticket.getFlight().getDestination());
        dto.setDepartureTime(ticket.getFlight().getDepartureTime());
        dto.setArrivalTime(ticket.getFlight().getArrivalTime());

        return dto;
    }
}