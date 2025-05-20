package ps.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ps.backend.model.Notification;
import ps.backend.model.User;
import ps.backend.model.Ticket;
import ps.backend.model.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser(User user);

    List<Notification> findByUserAndSeen(User user, boolean seen);

    List<Notification> findByTicket(Ticket ticket);

    List<Notification> findByType(NotificationType type);

    List<Notification> findByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end);
}