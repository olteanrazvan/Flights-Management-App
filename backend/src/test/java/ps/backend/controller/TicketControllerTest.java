package ps.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ps.backend.dto.TicketDTO;
import ps.backend.dto.UserDTO;
import ps.backend.model.Role;
import ps.backend.model.TicketStatus;
import ps.backend.service.PDFService;
import ps.backend.service.TicketService;
import ps.backend.service.UserService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @Mock
    private UserService userService;

    @Mock
    private PDFService pdfService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TicketController ticketController;

    private UserDTO testUser;
    private UserDTO adminUser;
    private TicketDTO testTicket;
    private List<TicketDTO> tickets;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up security context mock
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Create test users
        testUser = new UserDTO();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.CLIENT);

        adminUser = new UserDTO();
        adminUser.setId(2L);
        adminUser.setEmail("admin@example.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(Role.ADMIN);

        // Create test ticket
        testTicket = new TicketDTO();
        testTicket.setId(1L);
        testTicket.setTicketNumber("TKT123");
        testTicket.setFlightId(1L);
        testTicket.setUserId(testUser.getId());
        testTicket.setPassengerName("Test User");
        testTicket.setPassengerEmail("test@example.com");
        testTicket.setPrice(new BigDecimal("500.00"));
        testTicket.setPurchaseTime(LocalDateTime.now());
        testTicket.setSeatNumber("A1");
        testTicket.setStatus(TicketStatus.CONFIRMED);
        testTicket.setFlightNumber("FL123");
        testTicket.setOrigin("New York");
        testTicket.setDestination("London");
        testTicket.setDepartureTime(LocalDateTime.of(2025, 6, 1, 10, 0));
        testTicket.setArrivalTime(LocalDateTime.of(2025, 6, 1, 22, 0));

        // Create list of tickets
        tickets = new ArrayList<>();
        tickets.add(testTicket);

        // Create a second ticket for admin user
        TicketDTO ticket2 = new TicketDTO();
        ticket2.setId(2L);
        ticket2.setTicketNumber("TKT456");
        ticket2.setFlightId(2L);
        ticket2.setUserId(adminUser.getId());
        ticket2.setPassengerName("Admin User");
        ticket2.setPassengerEmail("admin@example.com");
        ticket2.setPrice(new BigDecimal("350.00"));
        ticket2.setPurchaseTime(LocalDateTime.now());
        ticket2.setSeatNumber("B2");
        ticket2.setStatus(TicketStatus.RESERVED);
        ticket2.setFlightNumber("FL456");
        tickets.add(ticket2);
    }

    @Test
    void getTicketById_shouldReturnTicket_whenUserIsTicketOwner() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock services to return user and ticket
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(testTicket.getId())).thenReturn(Optional.of(testTicket));

        // Call the controller method
        ResponseEntity<TicketDTO> response = ticketController.getTicketById(testTicket.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTicket.getId(), response.getBody().getId());
        assertEquals(testTicket.getTicketNumber(), response.getBody().getTicketNumber());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(ticketService).getTicketById(testTicket.getId());
    }

    @Test
    void getTicketById_shouldReturnTicket_whenUserIsAdmin() {
        // Set up the authentication to return the admin user's email
        when(authentication.getName()).thenReturn(adminUser.getEmail());

        // Mock services to return admin user and ticket
        when(userService.getUserByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));
        when(ticketService.getTicketById(testTicket.getId())).thenReturn(Optional.of(testTicket));

        // Call the controller method
        ResponseEntity<TicketDTO> response = ticketController.getTicketById(testTicket.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTicket.getId(), response.getBody().getId());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(adminUser.getEmail());
        verify(ticketService).getTicketById(testTicket.getId());
    }

    @Test
    void getTicketById_shouldReturnForbidden_whenUserIsNotAuthorized() {
        // Create another client user
        UserDTO otherUser = new UserDTO();
        otherUser.setId(3L);
        otherUser.setEmail("other@example.com");
        otherUser.setRole(Role.CLIENT);

        // Set up the authentication to return the other user's email
        when(authentication.getName()).thenReturn(otherUser.getEmail());

        // Mock services to return other user and ticket
        when(userService.getUserByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(ticketService.getTicketById(testTicket.getId())).thenReturn(Optional.of(testTicket));

        // Call the controller method
        ResponseEntity<TicketDTO> response = ticketController.getTicketById(testTicket.getId());

        // Verify the response is forbidden
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(otherUser.getEmail());
        verify(ticketService).getTicketById(testTicket.getId());
    }

    @Test
    void getTicketById_shouldReturnNotFound_whenTicketDoesNotExist() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user but no ticket
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(999L)).thenReturn(Optional.empty());

        // Call the controller method
        ResponseEntity<TicketDTO> response = ticketController.getTicketById(999L);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(ticketService).getTicketById(999L);
    }

    @Test
    void getTicketByNumber_shouldReturnTicket_whenUserIsTicketOwner() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock services to return user and ticket
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketByNumber(testTicket.getTicketNumber())).thenReturn(Optional.of(testTicket));

        // Call the controller method
        ResponseEntity<TicketDTO> response = ticketController.getTicketByNumber(testTicket.getTicketNumber());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testTicket.getId(), response.getBody().getId());
        assertEquals(testTicket.getTicketNumber(), response.getBody().getTicketNumber());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(ticketService).getTicketByNumber(testTicket.getTicketNumber());
    }

    @Test
    void getMyTickets_shouldReturnUserTickets() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create list of user's tickets
        List<TicketDTO> userTickets = new ArrayList<>();
        userTickets.add(testTicket);

        // Mock service to return user's tickets
        when(ticketService.getTicketsByUser(testUser.getId())).thenReturn(userTickets);

        // Call the controller method
        ResponseEntity<List<TicketDTO>> response = ticketController.getMyTickets();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(ticketService).getTicketsByUser(testUser.getId());
    }

    @Test
    void getTicketsByUser_shouldReturnUserTickets_whenUserIsAdmin() {
        // Mock service to return user's tickets
        when(ticketService.getTicketsByUser(testUser.getId())).thenReturn(List.of(testTicket));

        // Call the controller method
        ResponseEntity<List<TicketDTO>> response = ticketController.getTicketsByUser(testUser.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());

        // Verify the service was called correctly
        verify(ticketService).getTicketsByUser(testUser.getId());
    }

    @Test
    void getTicketsByFlight_shouldReturnFlightTickets_whenUserIsAdmin() {
        Long flightId = 1L;

        // Mock service to return flight's tickets
        when(ticketService.getTicketsByFlight(flightId)).thenReturn(List.of(testTicket));

        // Call the controller method
        ResponseEntity<List<TicketDTO>> response = ticketController.getTicketsByFlight(flightId);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());

        // Verify the service was called correctly
        verify(ticketService).getTicketsByFlight(flightId);
    }

    @Test
    void createTicket_shouldCreateTicket() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create new ticket DTO for creation (without user ID)
        TicketDTO newTicket = new TicketDTO();
        newTicket.setFlightId(1L);
        newTicket.setPassengerName("Test User");
        newTicket.setPassengerEmail("test@example.com");
        newTicket.setSeatNumber("C3");

        // Set ID to simulate creation
        TicketDTO createdTicket = new TicketDTO();
        createdTicket.setId(3L);
        createdTicket.setTicketNumber("TKT789");
        createdTicket.setFlightId(newTicket.getFlightId());
        createdTicket.setUserId(testUser.getId());
        createdTicket.setPassengerName(newTicket.getPassengerName());
        createdTicket.setPassengerEmail(newTicket.getPassengerEmail());
        createdTicket.setPrice(new BigDecimal("450.00"));
        createdTicket.setPurchaseTime(LocalDateTime.now());
        createdTicket.setSeatNumber(newTicket.getSeatNumber());
        createdTicket.setStatus(TicketStatus.RESERVED);

        // Mock service to return the created ticket
        when(ticketService.createTicket(any(TicketDTO.class))).thenReturn(createdTicket);

        // Call the controller method
        ResponseEntity<TicketDTO> response = ticketController.createTicket(newTicket);

        // Verify the response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdTicket.getId(), response.getBody().getId());
        assertEquals(createdTicket.getTicketNumber(), response.getBody().getTicketNumber());
        assertEquals(testUser.getId(), response.getBody().getUserId());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(ticketService).createTicket(any(TicketDTO.class));
    }

    @Test
    void confirmTicket_shouldConfirmTicket_whenUserIsTicketOwner() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock services to return user and ticket
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(testTicket.getId())).thenReturn(Optional.of(testTicket));

        // Create confirmed ticket
        TicketDTO confirmedTicket = new TicketDTO();
        confirmedTicket.setId(testTicket.getId());
        confirmedTicket.setTicketNumber(testTicket.getTicketNumber());
        confirmedTicket.setStatus(TicketStatus.CONFIRMED);
        confirmedTicket.setUserId(testUser.getId());

        // Mock service to return the confirmed ticket
        when(ticketService.confirmTicket(testTicket.getId())).thenReturn(confirmedTicket);

        // Call the controller method
        ResponseEntity<TicketDTO> response = ticketController.confirmTicket(testTicket.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TicketStatus.CONFIRMED, response.getBody().getStatus());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(ticketService).getTicketById(testTicket.getId());
        verify(ticketService).confirmTicket(testTicket.getId());
    }

    @Test
    void cancelTicket_shouldCancelTicket_whenUserIsTicketOwner() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock services to return user and ticket
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(testTicket.getId())).thenReturn(Optional.of(testTicket));

        // Create cancelled ticket
        TicketDTO cancelledTicket = new TicketDTO();
        cancelledTicket.setId(testTicket.getId());
        cancelledTicket.setTicketNumber(testTicket.getTicketNumber());
        cancelledTicket.setStatus(TicketStatus.CANCELLED);
        cancelledTicket.setUserId(testUser.getId());

        // Mock service to return the cancelled ticket
        when(ticketService.cancelTicket(testTicket.getId())).thenReturn(cancelledTicket);

        // Call the controller method
        ResponseEntity<TicketDTO> response = ticketController.cancelTicket(testTicket.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TicketStatus.CANCELLED, response.getBody().getStatus());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(ticketService).getTicketById(testTicket.getId());
        verify(ticketService).cancelTicket(testTicket.getId());
    }

    @Test
    void getTicketsByStatus_shouldReturnTicketsWithStatus() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create list of confirmed tickets
        List<TicketDTO> confirmedTickets = new ArrayList<>();
        confirmedTickets.add(testTicket);

        // Mock service to return tickets by status
        when(ticketService.getTicketsByStatus(testUser.getId(), TicketStatus.CONFIRMED)).thenReturn(confirmedTickets);

        // Call the controller method
        ResponseEntity<List<TicketDTO>> response = ticketController.getTicketsByStatus(TicketStatus.CONFIRMED);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testTicket.getId(), response.getBody().get(0).getId());
        assertEquals(TicketStatus.CONFIRMED, response.getBody().get(0).getStatus());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(ticketService).getTicketsByStatus(testUser.getId(), TicketStatus.CONFIRMED);
    }

    @Test
    void downloadTicketPdf_shouldReturnPdf_whenUserIsTicketOwner() throws IOException {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock services to return user and ticket
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(ticketService.getTicketById(testTicket.getId())).thenReturn(Optional.of(testTicket));

        // Create sample PDF bytes
        byte[] pdfBytes = "Sample PDF Content".getBytes();

        // Mock PDF service to return the PDF
        when(pdfService.generateTicketPDF(testTicket)).thenReturn(pdfBytes);

        // Call the controller method
        ResponseEntity<byte[]> response = ticketController.downloadTicketPdf(testTicket.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentDisposition().toString()
                .contains("attachment; filename=\"ticket_" + testTicket.getTicketNumber() + ".pdf\""));
        assertArrayEquals(pdfBytes, response.getBody());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(ticketService).getTicketById(testTicket.getId());
        verify(pdfService).generateTicketPDF(testTicket);
    }

    @Test
    void downloadTicketPdf_shouldReturnForbidden_whenUserIsNotAuthorized() throws IOException {
        // Create another client user
        UserDTO otherUser = new UserDTO();
        otherUser.setId(3L);
        otherUser.setEmail("other@example.com");
        otherUser.setRole(Role.CLIENT);

        // Set up the authentication to return the other user's email
        when(authentication.getName()).thenReturn(otherUser.getEmail());

        // Mock services to return other user and ticket
        when(userService.getUserByEmail(otherUser.getEmail())).thenReturn(Optional.of(otherUser));
        when(ticketService.getTicketById(testTicket.getId())).thenReturn(Optional.of(testTicket));

        // Call the controller method
        ResponseEntity<byte[]> response = ticketController.downloadTicketPdf(testTicket.getId());

        // Verify the response is forbidden
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(otherUser.getEmail());
        verify(ticketService).getTicketById(testTicket.getId());
        // generateTicketPDF should not be called since authorization check fails
        verify(pdfService, never()).generateTicketPDF(any());
    }
}