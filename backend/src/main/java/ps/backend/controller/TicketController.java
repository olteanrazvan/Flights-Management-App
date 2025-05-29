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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketDTO>> getAllTickets() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketDTO> getTicketById(@PathVariable Long id) {
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

        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<TicketDTO> getTicketByNumber(@PathVariable String ticketNumber) {
        Optional<TicketDTO> ticketOpt = ticketService.getTicketByNumber(ticketNumber);

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

        return ResponseEntity.ok(ticket);
    }

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

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketDTO>> getTicketsByUser(@PathVariable Long userId) {
        List<TicketDTO> tickets = ticketService.getTicketsByUser(userId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/flight/{flightId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketDTO>> getTicketsByFlight(@PathVariable Long flightId) {
        List<TicketDTO> tickets = ticketService.getTicketsByFlight(flightId);
        return ResponseEntity.ok(tickets);
    }

    @PostMapping
    public ResponseEntity<TicketDTO> createTicket(@RequestBody TicketDTO ticketDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        ticketDTO.setUserId(userOpt.get().getId());

        TicketDTO createdTicket = ticketService.createTicket(ticketDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTicket);
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<TicketDTO> confirmTicket(@PathVariable Long id) {
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

        if (!ticket.getUserId().equals(userOpt.get().getId()) && userOpt.get().getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        TicketDTO confirmedTicket = ticketService.confirmTicket(id);
        return ResponseEntity.ok(confirmedTicket);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<TicketDTO> cancelTicket(@PathVariable Long id) {
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

        if (!ticket.getUserId().equals(userOpt.get().getId()) && userOpt.get().getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        TicketDTO cancelledTicket = ticketService.cancelTicket(id);
        return ResponseEntity.ok(cancelledTicket);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketDTO> updateTicket(@PathVariable Long id, @RequestBody TicketDTO ticketDTO) {
        TicketDTO updatedTicket = ticketService.updateTicket(id, ticketDTO);
        return ResponseEntity.ok(updatedTicket);
    }

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