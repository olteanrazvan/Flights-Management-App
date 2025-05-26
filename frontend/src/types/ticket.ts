export enum TicketStatus {
    RESERVED = 'RESERVED',
    CONFIRMED = 'CONFIRMED',
    CANCELLED = 'CANCELLED'
}

export interface Ticket {
    id: number;
    ticketNumber: string;
    flightId: number;
    userId: number;
    passengerName: string;
    passengerEmail: string;
    price: number;
    purchaseTime: string;
    seatNumber: string;
    status: TicketStatus;
    // Flight details for convenience
    flightNumber?: string;
    origin?: string;
    destination?: string;
    departureTime?: string;
    arrivalTime?: string;
}

export interface BookingRequest {
    flightId: number;
    passengerName: string;
    passengerEmail: string;
    seatNumber: string;
    price?: number;
}