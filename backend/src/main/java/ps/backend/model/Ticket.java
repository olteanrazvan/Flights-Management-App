package ps.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String passengerName;

    @Column(nullable = false)
    private String passengerEmail;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private LocalDateTime purchaseTime;

    @Column(nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    // Default constructor
    public Ticket() {
        this.ticketNumber = UUID.randomUUID().toString();
        this.purchaseTime = LocalDateTime.now();
        this.status = TicketStatus.RESERVED;
    }

    // Parameterized constructor
    public Ticket(Flight flight, User user, String passengerName, String passengerEmail,
                  BigDecimal price, String seatNumber) {
        this();
        this.flight = flight;
        this.user = user;
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.price = price;
        this.seatNumber = seatNumber;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(LocalDateTime purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    // Helper method
    public void confirmTicket() {
        this.status = TicketStatus.CONFIRMED;
    }

    public void cancelTicket() {
        this.status = TicketStatus.CANCELLED;
        if (this.flight != null) {
            this.flight.incrementAvailableSeats();
        }
    }
}