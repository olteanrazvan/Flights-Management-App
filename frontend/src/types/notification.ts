export enum NotificationType {
    TICKET_CONFIRMATION = 'TICKET_CONFIRMATION',
    TICKET_CANCELLATION = 'TICKET_CANCELLATION',
    FLIGHT_SCHEDULE_CHANGE = 'FLIGHT_SCHEDULE_CHANGE',
    FLIGHT_DELAY = 'FLIGHT_DELAY',
    FLIGHT_CANCELLATION = 'FLIGHT_CANCELLATION',
    GENERAL = 'GENERAL'
}

export interface Notification {
    id: number;
    userId: number;
    message: string;
    seen: boolean;
    createdAt: string;
    ticketId?: number;
    type: NotificationType;
    ticketNumber?: string;
    flightNumber?: string;
}