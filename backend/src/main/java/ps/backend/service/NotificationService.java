package ps.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ps.backend.dto.NotificationDTO;
import ps.backend.model.Notification;
import ps.backend.model.NotificationType;
import ps.backend.model.Ticket;
import ps.backend.model.User;
import ps.backend.observer.TicketEventType;
import ps.backend.observer.TicketObserver;
import ps.backend.repository.NotificationRepository;
import ps.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service that observes ticket events and creates notifications
 */
@Service
public class NotificationService implements TicketObserver {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Email service could be injected here if available
    // private final EmailService emailService;

    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void update(Ticket ticket, TicketEventType eventType) {
        String message;
        NotificationType notificationType;

        // Create appropriate message and notification type based on event
        switch (eventType) {
            case CREATED:
                message = "Your ticket #" + ticket.getTicketNumber() + " for flight " +
                        ticket.getFlight().getFlightNumber() + " has been created. Departing from " +
                        ticket.getFlight().getOrigin() + " to " + ticket.getFlight().getDestination() +
                        " on " + ticket.getFlight().getDepartureTime().toLocalDate() + ".";
                notificationType = NotificationType.TICKET_CONFIRMATION;
                break;
            case CONFIRMED:
                message = "Your ticket #" + ticket.getTicketNumber() + " for flight " +
                        ticket.getFlight().getFlightNumber() + " has been confirmed. Seat number: " +
                        ticket.getSeatNumber() + ". Please arrive at the airport at least 2 hours before departure.";
                notificationType = NotificationType.TICKET_CONFIRMATION;
                break;
            case CANCELLED:
                message = "Your ticket #" + ticket.getTicketNumber() + " for flight " +
                        ticket.getFlight().getFlightNumber() + " has been cancelled. " +
                        "If you did not cancel this ticket, please contact customer support.";
                notificationType = NotificationType.TICKET_CANCELLATION;
                break;
            case UPDATED:
                message = "Your ticket #" + ticket.getTicketNumber() + " for flight " +
                        ticket.getFlight().getFlightNumber() + " has been updated. " +
                        "Please check your account for details.";
                notificationType = NotificationType.GENERAL;
                break;
            default:
                message = "There has been an update to your ticket #" + ticket.getTicketNumber();
                notificationType = NotificationType.GENERAL;
                break;
        }

        // Create and save notification
        Notification notification = new Notification(ticket.getUser(), message, notificationType, ticket);
        notificationRepository.save(notification);

        // Send email notification
        sendEmail(ticket.getPassengerEmail(), message);
    }

    /**
     * Send an email notification to the user
     * @param email The recipient's email address
     * @param message The notification message
     */
    private void sendEmail(String email, String message) {
        // In a real application, this would connect to an email service
        System.out.println("Sending email to " + email + ": " + message);

        // Implementation would typically use JavaMailSender or a third-party service
        // if (emailService != null) {
        //     emailService.sendSimpleMessage(email, "Flight Notification", message);
        // }
    }

    /**
     * Mark a notification as seen
     * @param notificationId The ID of the notification to mark as seen
     * @return The updated notification DTO
     */
    @Transactional
    public Optional<NotificationDTO> markAsSeen(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .map(notification -> {
                    notification.markAsSeen();
                    Notification savedNotification = notificationRepository.save(notification);
                    return convertToDTO(savedNotification);
                });
    }

    /**
     * Get all notifications for a user
     * @param userId The ID of the user
     * @return A list of notifications for the user
     */
    public List<NotificationDTO> getNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a user
     * @param userId The ID of the user
     * @return A list of unread notifications for the user
     */
    public List<NotificationDTO> getUnseenNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findByUserAndSeen(user, false).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get notifications for a specific ticket
     * @param ticketId The ID of the ticket
     * @return A list of notifications for the ticket
     */
    public List<NotificationDTO> getNotificationsForTicket(Long ticketId) {
        // Implementation would need a Ticket repository or service to find the ticket
        // For now, returning an empty list
        return List.of();
    }

    /**
     * Get notifications created between specific dates
     * @param userId The ID of the user
     * @param start Start date and time
     * @param end End date and time
     * @return A list of notifications created in the specified time range
     */
    public List<NotificationDTO> getNotificationsByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findByUserAndCreatedAtBetween(user, start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get notifications by type
     * @param type The notification type
     * @return A list of notifications of the specified type
     */
    public List<NotificationDTO> getNotificationsByType(NotificationType type) {
        return notificationRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Notification entity to NotificationDTO
     * @param notification The notification entity
     * @return NotificationDTO
     */
    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setMessage(notification.getMessage());
        dto.setSeen(notification.isSeen());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setType(notification.getType());

        // Include ticket information if available
        if (notification.getTicket() != null) {
            dto.setTicketId(notification.getTicket().getId());
            dto.setTicketNumber(notification.getTicket().getTicketNumber());
            dto.setFlightNumber(notification.getTicket().getFlight().getFlightNumber());
        }

        return dto;
    }
}