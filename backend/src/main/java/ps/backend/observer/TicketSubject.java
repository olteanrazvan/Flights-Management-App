package ps.backend.observer;

import ps.backend.model.Ticket;

/**
 * Subject interface for the Observer pattern
 * This interface should be implemented by classes that want to notify observers
 * about ticket-related events
 */
public interface TicketSubject {

    /**
     * Register an observer to receive notifications
     * @param observer The observer to register
     */
    void registerObserver(TicketObserver observer);

    /**
     * Remove an observer from the notification list
     * @param observer The observer to remove
     */
    void removeObserver(TicketObserver observer);

    /**
     * Notify all registered observers about a ticket event
     * @param ticket The ticket object related to the event
     * @param eventType The type of event that occurred
     */
    void notifyObservers(Ticket ticket, TicketEventType eventType);
}