import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Notification } from '../types/notification';
import { notificationService } from '../services/notificationService';
import { useAuth } from './AuthContext';

interface NotificationContextType {
    notifications: Notification[];
    unseenCount: number;
    isLoading: boolean;
    refreshNotifications: () => Promise<void>;
    markAsSeen: (id: number) => Promise<void>;
    markAllAsSeen: () => Promise<void>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

interface NotificationProviderProps {
    children: ReactNode;
}

export const NotificationProvider: React.FC<NotificationProviderProps> = ({ children }) => {
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const { isAuthenticated } = useAuth();

    const refreshNotifications = async (): Promise<void> => {
        if (!isAuthenticated) return;

        setIsLoading(true);
        try {
            const data = await notificationService.getMyNotifications();
            setNotifications(data);
        } catch (error) {
            console.error('Failed to fetch notifications:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const markAsSeen = async (id: number): Promise<void> => {
        try {
            await notificationService.markNotificationAsSeen(id);
            setNotifications(prev =>
                prev.map(notif =>
                    notif.id === id ? { ...notif, seen: true } : notif
                )
            );
        } catch (error) {
            console.error('Failed to mark notification as seen:', error);
        }
    };

    const markAllAsSeen = async (): Promise<void> => {
        try {
            await notificationService.markAllNotificationsSeen();
            setNotifications(prev =>
                prev.map(notif => ({ ...notif, seen: true }))
            );
        } catch (error) {
            console.error('Failed to mark all notifications as seen:', error);
        }
    };

    useEffect(() => {
        if (isAuthenticated) {
            refreshNotifications();
            // Refresh notifications every 30 seconds
            const interval = setInterval(refreshNotifications, 30000);
            return () => clearInterval(interval);
        } else {
            setNotifications([]);
        }
    }, [isAuthenticated]);

    const unseenCount = notifications.filter(n => !n.seen).length;

    const value: NotificationContextType = {
        notifications,
        unseenCount,
        isLoading,
        refreshNotifications,
        markAsSeen,
        markAllAsSeen,
    };

    return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
};

export const useNotifications = (): NotificationContextType => {
    const context = useContext(NotificationContext);
    if (context === undefined) {
        throw new Error('useNotifications must be used within a NotificationProvider');
    }
    return context;
};