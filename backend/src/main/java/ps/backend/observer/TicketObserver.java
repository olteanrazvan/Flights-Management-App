package ps.backend.observer;

import ps.backend.model.Ticket;

/**
 * Observer interface for the Observer pattern
 * This interface should be implemented by classes that want to be notified
 * about ticket-related events
 */
public interface TicketObserver {

    /**
     * This method is called when a ticket event occurs
     * @param ticket The ticket object related to the event
     * @param eventType The type of event that occurred
     */
    void update(Ticket ticket, TicketEventType eventType);
}