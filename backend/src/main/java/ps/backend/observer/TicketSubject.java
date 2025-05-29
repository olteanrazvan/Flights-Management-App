package ps.backend.observer;

import ps.backend.model.Ticket;

public interface TicketSubject {

    void registerObserver(TicketObserver observer);

    void removeObserver(TicketObserver observer);

    void notifyObservers(Ticket ticket, TicketEventType eventType);
}