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

    @PostMapping("/{id}/mark-seen")
    public ResponseEntity<NotificationDTO> markNotificationAsSeen(@PathVariable Long id) {
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

    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByType(@PathVariable NotificationType type) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByType(type);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForUser(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/ticket/{ticketId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsForTicket(@PathVariable Long ticketId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        if (userOpt.get().getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).build();
        }

        List<NotificationDTO> notifications = notificationService.getNotificationsForTicket(ticketId);
        return ResponseEntity.ok(notifications);
    }

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

    @PostMapping("/mark-all-seen")
    public ResponseEntity<?> markAllNotificationsSeen() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Optional<UserDTO> userOpt = userService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        List<NotificationDTO> unseenNotifications = notificationService.getUnseenNotificationsForUser(userOpt.get().getId());

        for (NotificationDTO notification : unseenNotifications) {
            notificationService.markAsSeen(notification.getId());
        }

        return ResponseEntity.ok().build();
    }
}