export interface Flight {
    id: number;
    flightNumber: string;
    origin: string;
    destination: string;
    departureTime: string;
    arrivalTime: string;
    totalSeats: number;
    availableSeats: number;
    basePrice: number;
    ticketIds?: number[];
}

export interface FlightSearchParams {
    origin: string;
    destination: string;
    departureDate: string;
    passengers?: number;
}
