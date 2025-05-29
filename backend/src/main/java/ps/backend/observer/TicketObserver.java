package ps.backend.observer;

import ps.backend.model.Ticket;

public interface TicketObserver {
    void update(Ticket ticket, TicketEventType eventType);
}