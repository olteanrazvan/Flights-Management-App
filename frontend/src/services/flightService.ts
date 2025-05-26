import api from './api';
import { Flight, FlightSearchParams } from '../types/flight';

export const flightService = {
    async getAllFlights(): Promise<Flight[]> {
        const response = await api.get<Flight[]>('/flights');
        return response.data;
    },

    async getFlightById(id: number): Promise<Flight> {
        const response = await api.get<Flight>(`/flights/${id}`);
        return response.data;
    },

    async searchFlights(params: FlightSearchParams): Promise<Flight[]> {
        const response = await api.get<Flight[]>('/flights/search', { params });
        return response.data;
    },

    async createFlight(flight: Omit<Flight, 'id' | 'ticketIds'>): Promise<Flight> {
        const response = await api.post<Flight>('/flights', flight);
        return response.data;
    },

    async updateFlight(id: number, flight: Partial<Flight>): Promise<Flight> {
        const response = await api.put<Flight>(`/flights/${id}`, flight);
        return response.data;
    },

    async deleteFlight(id: number): Promise<void> {
        await api.delete(`/flights/${id}`);
    },
};