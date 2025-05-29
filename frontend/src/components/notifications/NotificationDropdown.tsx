import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Box,
    List,
    ListItem,
    ListItemText,
    Typography,
    Chip,
    IconButton,
    Divider,
    Button,
} from '@mui/material';
import {
    MarkAsUnread,
    CheckCircle,
    Cancel,
    Info,
    Flight,
    Schedule,
} from '@mui/icons-material';
import { Notification, NotificationType } from '../../types/notification';
import { useNotifications } from '../../context/NotificationContext';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

interface NotificationDropdownProps {
    notifications: Notification[];
    onViewAll: () => void;
    onClose: () => void;
}

export const NotificationDropdown: React.FC<NotificationDropdownProps> = ({
                                                                              notifications,
                                                                              onViewAll,
                                                                              onClose,
                                                                          }) => {
    const { markAsSeen, markAllAsSeen } = useNotifications();

    const getNotificationIcon = (type: NotificationType) => {
        switch (type) {
            case NotificationType.TICKET_CONFIRMATION:
                return <CheckCircle color="success" />;
            case NotificationType.TICKET_CANCELLATION:
                return <Cancel color="error" />;
            case NotificationType.FLIGHT_SCHEDULE_CHANGE:
            case NotificationType.FLIGHT_DELAY:
            case NotificationType.FLIGHT_CANCELLATION:
                return <Flight color="warning" />;
            default:
                return <Info color="info" />;
        }
    };

    const handleNotificationClick = async (notification: Notification) => {
        if (!notification.seen) {
            await markAsSeen(notification.id);
        }
    };

    const handleMarkAllAsSeen = async () => {
        await markAllAsSeen();
    };

    const formatTime = (dateString: string) => {
        return dayjs(dateString).fromNow();
    };

    const unseenNotifications = notifications.filter(n => !n.seen);
    const displayNotifications = notifications.slice(0, 5); // Show only 5 recent notifications

    return (
        <Box sx={{ width: 350, maxHeight: 400 }}>
            {/* Header */}
            <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
                <Box display="flex" justifyContent="space-between" alignItems="center">
                    <Typography variant="h6" fontWeight="bold">
                        Notifications
                    </Typography>
                    {unseenNotifications.length > 0 && (
                        <Chip
                            label={`${unseenNotifications.length} new`}
                            color="primary"
                            size="small"
                        />
                    )}
                </Box>
                {unseenNotifications.length > 0 && (
                    <Button
                        size="small"
                        onClick={handleMarkAllAsSeen}
                        sx={{ mt: 1 }}
                    >
                        Mark all as read
                    </Button>
                )}
            </Box>

            {/* Notifications List */}
            <List sx={{ p: 0, maxHeight: 300, overflow: 'auto' }}>
                {displayNotifications.length === 0 ? (
                    <ListItem>
                        <ListItemText
                            primary="No notifications"
                            secondary="You're all caught up!"
                            sx={{ textAlign: 'center' }}
                        />
                    </ListItem>
                ) : (
                    displayNotifications.map((notification, index) => (
                        <React.Fragment key={notification.id}>
                            <ListItem
                                button
                                onClick={() => handleNotificationClick(notification)}
                                sx={{
                                    backgroundColor: notification.seen ? 'transparent' : 'action.hover',
                                    '&:hover': {
                                        backgroundColor: 'action.selected',
                                    },
                                }}
                            >
                                <Box sx={{ mr: 2 }}>
                                    {getNotificationIcon(notification.type)}
                                </Box>
                                <ListItemText
                                    primary={
                                        <Box>
                                            <Typography
                                                variant="body2"
                                                sx={{
                                                    fontWeight: notification.seen ? 'normal' : 'bold',
                                                    mb: 0.5,
                                                }}
                                            >
                                                {notification.message}
                                            </Typography>
                                            {notification.flightNumber && (
                                                <Chip
                                                    label={`Flight ${notification.flightNumber}`}
                                                    size="small"
                                                    variant="outlined"
                                                    sx={{ mr: 1 }}
                                                />
                                            )}
                                        </Box>
                                    }
                                    secondary={
                                        <Box display="flex" alignItems="center" sx={{ mt: 1 }}>
                                            <Schedule sx={{ fontSize: 12, mr: 0.5 }} />
                                            <Typography variant="caption">
                                                {formatTime(notification.createdAt)}
                                            </Typography>
                                        </Box>
                                    }
                                />
                                {!notification.seen && (
                                    <Box sx={{ ml: 1 }}>
                                        <Box
                                            sx={{
                                                width: 8,
                                                height: 8,
                                                backgroundColor: 'primary.main',
                                                borderRadius: '50%',
                                            }}
                                        />
                                    </Box>
                                )}
                            </ListItem>
                            {index < displayNotifications.length - 1 && <Divider />}
                        </React.Fragment>
                    ))
                )}
            </List>

            {/* Footer */}
            {notifications.length > 5 && (
                <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
                    <Button
                        fullWidth
                        variant="text"
                        onClick={() => {
                            onViewAll();
                            onClose();
                        }}
                    >
                        View All Notifications ({notifications.length})
                    </Button>
                </Box>
            )}
        </Box>
    );
};