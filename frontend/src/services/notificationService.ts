import api from './api';
import { Notification, NotificationType } from '../types/notification';

export const notificationService = {
    async getMyNotifications(): Promise<Notification[]> {
        const response = await api.get<Notification[]>('/notifications');
        return response.data;
    },

    async getUnseenNotifications(): Promise<Notification[]> {
        const response = await api.get<Notification[]>('/notifications/unseen');
        return response.data;
    },

    async markNotificationAsSeen(id: number): Promise<Notification> {
        const response = await api.post<Notification>(`/notifications/${id}/mark-seen`);
        return response.data;
    },

    async markAllNotificationsSeen(): Promise<void> {
        await api.post('/notifications/mark-all-seen');
    },

    async getNotificationsByDateRange(start: string, end: string): Promise<Notification[]> {
        const response = await api.get<Notification[]>('/notifications/date-range', {
            params: { start, end },
        });
        return response.data;
    },

    // Admin only
    async getNotificationsByType(type: NotificationType): Promise<Notification[]> {
        const response = await api.get<Notification[]>(`/notifications/type/${type}`);
        return response.data;
    },

    async getNotificationsForUser(userId: number): Promise<Notification[]> {
        const response = await api.get<Notification[]>(`/notifications/user/${userId}`);
        return response.data;
    },
};