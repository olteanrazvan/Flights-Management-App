package ps.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import ps.backend.dto.TicketDTO;
import ps.backend.dto.UserDTO;
import ps.backend.model.Role;
import ps.backend.model.TicketStatus;
import ps.backend.service.PDFService;
import ps.backend.service.TicketService;
import ps.backend.service.UserService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;
    private final PDFService pdfService;

    @Autowired
    public TicketController(TicketService ticketService, UserService userService, PDFService pdfService) {
        this.ticketService = ticketService;
        this.userService = userService;
        this.pdfService = pdfService;
    }

    /**
     * Get all tickets (admin only)
     *
     * @return List of all tickets
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        // This would need to be implemented in the service
        return ResponseEntity.ok(List.of());
    }

    /**
     * Get ticket by ID (admin or ticket owner only)
     *
     * @param id Ticket ID
     * @return The ticket if found and authorized
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id) {
        Optional<TicketDTO> ticketOpt = ticketService.getTicketById(id);

        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TicketDTO ticket = ticketOpt.get();

        // Check if user is authorized to access this ticket
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        UserDTO user = userOpt.get();

        // Allow access if user is the ticket owner or an admin
        if (!ticket.getUserId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(ticket);
    }

    /**
     * Get ticket by ticket number
     *
     * @param ticketNumber Ticket number
     * @return The ticket if found
     */
    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<TicketDTO> getTicketByNumber(@PathVariable String ticketNumber) {
        Optional<TicketDTO> ticketOpt = ticketService.getTicketByNumber(ticketNumber);

        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TicketDTO ticket = ticketOpt.get();

        // Check if user is authorized to access this ticket
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        UserDTO user = userOpt.get();

        // Allow access if user is the ticket owner or an admin
        if (!ticket.getUserId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(ticket);
    }

    /**
     * Get all tickets for the current user
     *
     * @return List of tickets for the user
     */
    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketDTO>> getMyTickets() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<TicketDTO> tickets = ticketService.getTicketsByUser(userOpt.get().getId());
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get all tickets for a specific user (admin only)
     *
     * @param userId User ID
     * @return List of tickets for the user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketDTO>> getTicketsByUser(@PathVariable Long userId) {
        List<TicketDTO> tickets = ticketService.getTicketsByUser(userId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Get all tickets for a specific flight
     *
     * @param flightId Flight ID
     * @return List of tickets for the flight
     */
    @GetMapping("/flight/{flightId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketDTO>> getTicketsByFlight(@PathVariable Long flightId) {
        List<TicketDTO> tickets = ticketService.getTicketsByFlight(flightId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Create a new ticket
     *
     * @param ticketDTO DTO containing ticket details
     * @return The created ticket
     */
    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(@RequestBody TicketDTO ticketDTO) {
        // Set the user ID from the authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        // Set the user ID
        ticketDTO.setUserId(userOpt.get().getId());

        // Create the ticket
        TicketDTO createdTicket = ticketService.createTicket(ticketDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    /**
     * Confirm a ticket
     *
     * @param id Ticket ID
     * @return The confirmed ticket
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<TicketDTO> confirmTicket(@PathVariable Long id) {
        // Check if user is authorized
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        Optional<TicketDTO> ticketOpt = ticketService.getTicketById(id);

        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TicketDTO ticket = ticketOpt.get();

        // Only allow the ticket owner or an admin to confirm the ticket
        if (!ticket.getUserId().equals(userOpt.get().getId()) && userOpt.get().getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        TicketDTO confirmedTicket = ticketService.confirmTicket(id);
        return ResponseEntity.ok(confirmedTicket);
    }

    /**
     * Cancel a ticket
     *
     * @param id Ticket ID
     * @return The cancelled ticket
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TicketDTO> cancelTicket(@PathVariable Long id) {
        // Check if user is authorized
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        Optional<TicketDTO> ticketOpt = ticketService.getTicketById(id);

        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TicketDTO ticket = ticketOpt.get();

        // Only allow the ticket owner or an admin to cancel the ticket
        if (!ticket.getUserId().equals(userOpt.get().getId()) && userOpt.get().getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        TicketDTO cancelledTicket = ticketService.cancelTicket(id);
        return ResponseEntity.ok(cancelledTicket);
    }

    /**
     * Update a ticket
     *
     * @param id Ticket ID
     * @param ticketDTO DTO containing updated ticket details
     * @return The updated ticket
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketDTO> updateTicket(@PathVariable Long id, @RequestBody TicketDTO ticketDTO) {
        TicketDTO updatedTicket = ticketService.updateTicket(id, ticketDTO);
        return ResponseEntity.ok(updatedTicket);
    }

    /**
     * Get tickets by status for the current user
     *
     * @param status Ticket status
     * @return List of tickets with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketDTO>> getTicketsByStatus(@PathVariable TicketStatus status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<TicketDTO> tickets = ticketService.getTicketsByStatus(userOpt.get().getId(), status);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Download ticket as PDF
     *
     * @param id Ticket ID
     * @return PDF file
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadTicketPdf(@PathVariable Long id) {
        Optional<TicketDTO> ticketOpt = ticketService.getTicketById(id);

        if (ticketOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TicketDTO ticket = ticketOpt.get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        UserDTO user = userOpt.get();

        if (!ticket.getUserId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        try {
            byte[] pdfBytes = pdfService.generateTicketPDF(ticket);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                    "attachment",
                    "ticket_" + ticket.getTicketNumber() + ".pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}