import api from './api';
import { Ticket, BookingRequest, TicketStatus } from '../types/ticket';

export const ticketService = {
    async getMyTickets(): Promise<Ticket[]> {
        const response = await api.get<Ticket[]>('/tickets/my-tickets');
        return response.data;
    },

    async getTicketById(id: number): Promise<Ticket> {
        const response = await api.get<Ticket>(`/tickets/${id}`);
        return response.data;
    },

    async getTicketByNumber(ticketNumber: string): Promise<Ticket> {
        const response = await api.get<Ticket>(`/tickets/number/${ticketNumber}`);
        return response.data;
    },

    async createTicket(booking: BookingRequest): Promise<Ticket> {
        const response = await api.post<Ticket>('/tickets', booking);
        return response.data;
    },

    async confirmTicket(id: number): Promise<Ticket> {
        const response = await api.post<Ticket>(`/tickets/${id}/confirm`);
        return response.data;
    },

    async cancelTicket(id: number): Promise<Ticket> {
        const response = await api.post<Ticket>(`/tickets/${id}/cancel`);
        return response.data;
    },

    async getTicketsByStatus(status: TicketStatus): Promise<Ticket[]> {
        const response = await api.get<Ticket[]>(`/tickets/status/${status}`);
        return response.data;
    },

    async downloadTicketPdf(id: number): Promise<Blob> {
        const response = await api.get(`/tickets/${id}/pdf`, {
            responseType: 'blob',
        });
        return response.data;
    },

    // Admin only
    async getTicketsByUser(userId: number): Promise<Ticket[]> {
        const response = await api.get<Ticket[]>(`/tickets/user/${userId}`);
        return response.data;
    },

    async getTicketsByFlight(flightId: number): Promise<Ticket[]> {
        const response = await api.get<Ticket[]>(`/tickets/flight/${flightId}`);
        return response.data;
    },
};