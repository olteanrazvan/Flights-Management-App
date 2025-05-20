package ps.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import ps.backend.dto.NotificationDTO;
import ps.backend.dto.UserDTO;
import ps.backend.model.NotificationType;
import ps.backend.model.Role;
import ps.backend.service.NotificationService;
import ps.backend.service.UserService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    /**
     * Get all notifications for the current user
     *
     * @return List of notifications for the user
     */
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getUserNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<NotificationDTO> notifications = notificationService.getNotificationsForUser(userOpt.get().getId());
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get all unread notifications for the current user
     *
     * @return List of unread notifications for the user
     */
    @GetMapping("/unseen")
    public ResponseEntity<List<NotificationDTO>> getUnseenNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<NotificationDTO> notifications = notificationService.getUnseenNotificationsForUser(userOpt.get().getId());
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark a notification as read
     *
     * @param id Notification ID
     * @return The updated notification
     */
    @PostMapping("/{id}/mark-seen")
    public ResponseEntity<NotificationDTO> markNotificationAsSeen(@PathVariable Long id) {
        // Check if user is authorized to mark this notification as read
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        Optional<NotificationDTO> updatedNotification = notificationService.markAsSeen(id);

        return updatedNotification
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get notifications by type (admin only)
     *
     * @param type Notification type
     * @return List of notifications of the specified type
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByType(@PathVariable NotificationType type) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByType(type);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications for a specific user (admin only)
     *
     * @param userId User ID
     * @return List of notifications for the user
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForUser(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications for a specific ticket
     *
     * @param ticketId Ticket ID
     * @return List of notifications for the ticket
     */
    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForTicket(@PathVariable Long ticketId) {
        // Check if user is authorized to view these notifications
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        // For now, only admins can view notifications for a specific ticket
        // This could be modified to allow ticket owners to view their own ticket notifications
        if (userOpt.get().getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        List<NotificationDTO> notifications = notificationService.getNotificationsForTicket(ticketId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get notifications created between specific dates
     *
     * @param start Start date and time (ISO format)
     * @param end End date and time (ISO format)
     * @return List of notifications created in the specified time range
     */
    @GetMapping("/date-range")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByDateRange(
            @RequestParam String start,
            @RequestParam String end) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        try {
            LocalDateTime startDate = LocalDateTime.parse(start, DATE_TIME_FORMATTER);
            LocalDateTime endDate = LocalDateTime.parse(end, DATE_TIME_FORMATTER);

            List<NotificationDTO> notifications = notificationService.getNotificationsByDateRange(
                    userOpt.get().getId(), startDate, endDate);

            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Mark all notifications as read for the current user
     *
     * @return Success or failure response
     */
    @PostMapping("/mark-all-seen")
    public ResponseEntity<?> markAllNotificationsSeen() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        // Get all unseen notifications
        List<NotificationDTO> unseenNotifications = notificationService.getUnseenNotificationsForUser(userOpt.get().getId());

        // Mark each as seen
        for (NotificationDTO notification : unseenNotifications) {
            notificationService.markAsSeen(notification.getId());
        }

        return ResponseEntity.ok().build();
    }
}