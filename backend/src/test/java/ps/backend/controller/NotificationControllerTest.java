package ps.backend.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ps.backend.dto.NotificationDTO;
import ps.backend.dto.UserDTO;
import ps.backend.model.NotificationType;
import ps.backend.model.Role;
import ps.backend.service.NotificationService;
import ps.backend.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private NotificationController notificationController;

    private UserDTO testUser;
    private UserDTO adminUser;
    private NotificationDTO testNotification;
    private List<NotificationDTO> notifications;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

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

        // Create test notification
        testNotification = new NotificationDTO();
        testNotification.setId(1L);
        testNotification.setUserId(testUser.getId());
        testNotification.setMessage("Your ticket has been confirmed.");
        testNotification.setSeen(false);
        testNotification.setCreatedAt(LocalDateTime.now().minusHours(1));
        testNotification.setType(NotificationType.TICKET_CONFIRMATION);
        testNotification.setTicketId(1L);
        testNotification.setTicketNumber("TKT123");
        testNotification.setFlightNumber("FL123");

        // Create list of notifications
        notifications = new ArrayList<>();
        notifications.add(testNotification);

        // Create a second notification
        NotificationDTO notification2 = new NotificationDTO();
        notification2.setId(2L);
        notification2.setUserId(testUser.getId());
        notification2.setMessage("Your flight schedule has been changed.");
        notification2.setSeen(true);
        notification2.setCreatedAt(LocalDateTime.now().minusDays(1));
        notification2.setType(NotificationType.FLIGHT_SCHEDULE_CHANGE);
        notification2.setTicketId(1L);
        notification2.setTicketNumber("TKT123");
        notification2.setFlightNumber("FL123");
        notifications.add(notification2);
    }

    @Test
    void getUserNotifications_shouldReturnUserNotifications() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Mock service to return user's notifications
        when(notificationService.getNotificationsForUser(testUser.getId())).thenReturn(notifications);

        // Call the controller method
        ResponseEntity<List<NotificationDTO>> response = notificationController.getUserNotifications();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains(testNotification));

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(notificationService).getNotificationsForUser(testUser.getId());
    }

    @Test
    void getUserNotifications_shouldReturnNotFound_whenUserDoesNotExist() {
        // Set up the authentication to return a non-existent email
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        // Mock service to return empty
        when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Call the controller method
        ResponseEntity<List<NotificationDTO>> response = notificationController.getUserNotifications();

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        // Verify the service was called correctly
        verify(userService).getUserByEmail("nonexistent@example.com");
        // getNotificationsForUser should not be called
        verify(notificationService, never()).getNotificationsForUser(any());
    }

    @Test
    void getUnseenNotifications_shouldReturnUnseenNotifications() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create list of unseen notifications
        List<NotificationDTO> unseenNotifications = new ArrayList<>();
        unseenNotifications.add(testNotification); // Only the first notification is unseen

        // Mock service to return unseen notifications
        when(notificationService.getUnseenNotificationsForUser(testUser.getId())).thenReturn(unseenNotifications);

        // Call the controller method
        ResponseEntity<List<NotificationDTO>> response = notificationController.getUnseenNotifications();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testNotification.getId(), response.getBody().get(0).getId());
        assertFalse(response.getBody().get(0).isSeen());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(notificationService).getUnseenNotificationsForUser(testUser.getId());
    }

    @Test
    void markNotificationAsSeen_shouldMarkNotificationAsSeen() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create updated notification
        NotificationDTO seenNotification = new NotificationDTO();
        seenNotification.setId(testNotification.getId());
        seenNotification.setUserId(testNotification.getUserId());
        seenNotification.setMessage(testNotification.getMessage());
        seenNotification.setSeen(true); // Now seen
        seenNotification.setCreatedAt(testNotification.getCreatedAt());
        seenNotification.setType(testNotification.getType());
        seenNotification.setTicketId(testNotification.getTicketId());

        // Mock service to return the updated notification
        when(notificationService.markAsSeen(testNotification.getId())).thenReturn(Optional.of(seenNotification));

        // Call the controller method
        ResponseEntity<NotificationDTO> response = notificationController.markNotificationAsSeen(testNotification.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSeen());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(notificationService).markAsSeen(testNotification.getId());
    }

    @Test
    void markNotificationAsSeen_shouldReturnNotFound_whenNotificationDoesNotExist() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Mock service to return empty
        when(notificationService.markAsSeen(999L)).thenReturn(Optional.empty());

        // Call the controller method
        ResponseEntity<NotificationDTO> response = notificationController.markNotificationAsSeen(999L);

        // Verify the response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(notificationService).markAsSeen(999L);
    }

    @Test
    void getNotificationsByType_shouldReturnNotificationsOfType_whenUserIsAdmin() {
        // Create list of notifications of type TICKET_CONFIRMATION
        List<NotificationDTO> confirmationNotifications = new ArrayList<>();
        confirmationNotifications.add(testNotification);

        // Mock service to return notifications by type
        when(notificationService.getNotificationsByType(NotificationType.TICKET_CONFIRMATION))
                .thenReturn(confirmationNotifications);

        // Call the controller method
        ResponseEntity<List<NotificationDTO>> response =
                notificationController.getNotificationsByType(NotificationType.TICKET_CONFIRMATION);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(NotificationType.TICKET_CONFIRMATION, response.getBody().get(0).getType());

        // Verify the service was called correctly
        verify(notificationService).getNotificationsByType(NotificationType.TICKET_CONFIRMATION);
    }

    @Test
    void getNotificationsForUser_shouldReturnUserNotifications_whenUserIsAdmin() {
        // Mock service to return user's notifications
        when(notificationService.getNotificationsForUser(testUser.getId())).thenReturn(notifications);

        // Call the controller method
        ResponseEntity<List<NotificationDTO>> response = notificationController.getNotificationsForUser(testUser.getId());

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains(testNotification));

        // Verify the service was called correctly
        verify(notificationService).getNotificationsForUser(testUser.getId());
    }

    @Test
    void getNotificationsForTicket_shouldReturnTicketNotifications_whenUserIsAdmin() {
        // Create list of notifications for ticket
        List<NotificationDTO> ticketNotifications = new ArrayList<>();
        ticketNotifications.add(testNotification);

        // Mock service to return notifications for ticket
        when(notificationService.getNotificationsForTicket(1L)).thenReturn(ticketNotifications);

        // Set up the authentication to return the admin user's email
        when(authentication.getName()).thenReturn(adminUser.getEmail());

        // Mock service to return the admin user
        when(userService.getUserByEmail(adminUser.getEmail())).thenReturn(Optional.of(adminUser));

        // Call the controller method
        ResponseEntity<List<NotificationDTO>> response = notificationController.getNotificationsForTicket(1L);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getTicketId());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(adminUser.getEmail());
        verify(notificationService).getNotificationsForTicket(1L);
    }

    @Test
    void getNotificationsForTicket_shouldReturnForbidden_whenUserIsNotAdmin() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Call the controller method
        ResponseEntity<List<NotificationDTO>> response = notificationController.getNotificationsForTicket(1L);

        // Verify the response is forbidden
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());

        // Verify the service was called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        // getNotificationsForTicket should not be called since authorization check fails
        verify(notificationService, never()).getNotificationsForTicket(anyLong());
    }

    @Test
    void getNotificationsByDateRange_shouldReturnNotificationsInDateRange() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Define date range
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now();

        // Mock service to return notifications by date range
        when(notificationService.getNotificationsByDateRange(eq(testUser.getId()), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(notifications);

        // Call the controller method
        ResponseEntity<List<NotificationDTO>> response = notificationController.getNotificationsByDateRange(
                start.format(DATE_TIME_FORMATTER), end.format(DATE_TIME_FORMATTER));

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(notificationService).getNotificationsByDateRange(eq(testUser.getId()), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getNotificationsByDateRange_shouldReturnBadRequest_whenDateFormatIsInvalid() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Call the controller method with invalid date format
        ResponseEntity<List<NotificationDTO>> response = notificationController.getNotificationsByDateRange(
                "2025-06-01", "2025-06-10"); // Missing time part

        // Verify the response is bad request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());

        // Verify the service was called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        // getNotificationsByDateRange should not be called due to date parsing failure
        verify(notificationService, never()).getNotificationsByDateRange(anyLong(), any(), any());
    }

    @Test
    void markAllNotificationsSeen_shouldMarkAllNotificationsSeen() {
        // Set up the authentication to return the test user's email
        when(authentication.getName()).thenReturn(testUser.getEmail());

        // Mock service to return the test user
        when(userService.getUserByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Create list of unseen notifications
        List<NotificationDTO> unseenNotifications = new ArrayList<>();
        unseenNotifications.add(testNotification);

        // Mock service to return unseen notifications
        when(notificationService.getUnseenNotificationsForUser(testUser.getId())).thenReturn(unseenNotifications);

        // Mock service to mark notification as seen
        when(notificationService.markAsSeen(testNotification.getId())).thenReturn(Optional.of(testNotification));

        // Call the controller method
        ResponseEntity<?> response = notificationController.markAllNotificationsSeen();

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify the services were called correctly
        verify(userService).getUserByEmail(testUser.getEmail());
        verify(notificationService).getUnseenNotificationsForUser(testUser.getId());
        verify(notificationService).markAsSeen(testNotification.getId());
    }

    @Test
    void markAllNotificationsSeen_shouldReturnForbidden_whenUserDoesNotExist() {
        // Set up the authentication to return a non-existent email
        when(authentication.getName()).thenReturn("nonexistent@example.com");

        // Mock service to return empty
        when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Call the controller method
        ResponseEntity<?> response = notificationController.markAllNotificationsSeen();

        // Verify the response is forbidden
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNull(response.getBody());

        // Verify the service was called correctly
        verify(userService).getUserByEmail("nonexistent@example.com");
        // No further service calls should be made
        verify(notificationService, never()).getUnseenNotificationsForUser(anyLong());
        verify(notificationService, never()).markAsSeen(anyLong());
    }
}