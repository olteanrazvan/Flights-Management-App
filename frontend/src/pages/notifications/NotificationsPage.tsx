import React, { useState, useEffect } from 'react';
import {
    Container,
    Typography,
    Box,
    Alert,
    CircularProgress,
    Tabs,
    Tab,
    Stack,
    Button,
    Card,
    CardContent,
    Chip,
    IconButton,
    List,
    ListItem,
    ListItemText,
    ListItemIcon,
    Divider,
} from '@mui/material';
import {
    Notifications,
    Refresh,
    MarkAsUnread,
    CheckCircle,
    Cancel,
    Info,
    Flight,
    Schedule,
    Delete,
} from '@mui/icons-material';
import { useNotifications } from '../../context/NotificationContext';
import { Notification, NotificationType } from '../../types/notification';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
    return (
        <div role="tabpanel" hidden={value !== index}>
            {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
        </div>
    );
};

export const NotificationsPage: React.FC = () => {
    const {
        notifications,
        unseenCount,
        isLoading,
        refreshNotifications,
        markAsSeen,
        markAllAsSeen,
    } = useNotifications();

    const [activeTab, setActiveTab] = useState(0);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const loadNotifications = async () => {
            try {
                await refreshNotifications();
            } catch (err: any) {
                setError('Failed to load notifications. Please try again.');
            }
        };

        loadNotifications();
    }, []);

    const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
        setActiveTab(newValue);
    };

    const handleRefresh = async () => {
        setError(null);
        try {
            await refreshNotifications();
        } catch (err: any) {
            setError('Failed to refresh notifications. Please try again.');
        }
    };

    const handleMarkAsSeen = async (notificationId: number) => {
        try {
            await markAsSeen(notificationId);
        } catch (err: any) {
            setError('Failed to mark notification as read.');
        }
    };

    const handleMarkAllAsSeen = async () => {
        try {
            await markAllAsSeen();
        } catch (err: any) {
            setError('Failed to mark all notifications as read.');
        }
    };

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

    const filterNotificationsByStatus = (seen?: boolean) => {
        if (seen === undefined) return notifications;
        return notifications.filter(notification => notification.seen === seen);
    };

    const getNotificationCounts = () => {
        return {
            all: notifications.length,
            unread: notifications.filter(n => !n.seen).length,
            read: notifications.filter(n => n.seen).length,
        };
    };

    const counts = getNotificationCounts();

    const tabs = [
        { label: `All (${counts.all})`, status: undefined },
        { label: `Unread (${counts.unread})`, status: false },
        { label: `Read (${counts.read})`, status: true },
    ];

    const formatTime = (dateString: string) => {
        return dayjs(dateString).format('MMM DD, YYYY • HH:mm');
    };

    const formatRelativeTime = (dateString: string) => {
        return dayjs(dateString).fromNow();
    };

    const renderNotificationList = (filteredNotifications: Notification[]) => {
        if (filteredNotifications.length === 0) {
            return (
                <Box textAlign="center" py={8}>
                    <Notifications sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
                    <Typography variant="h6" color="text.secondary" gutterBottom>
                        No notifications found
                    </Typography>
                    <Typography variant="body2" color="text.secondary" mb={3}>
                        {activeTab === 0
                            ? "You don't have any notifications yet."
                            : `No ${tabs[activeTab].status === false ? 'unread' : 'read'} notifications.`
                        }
                    </Typography>
                </Box>
            );
        }

        return (
            <List sx={{ p: 0 }}>
                {filteredNotifications.map((notification, index) => (
                    <React.Fragment key={notification.id}>
                        <Card
                            variant="outlined"
                            sx={{
                                mb: 2,
                                backgroundColor: notification.seen ? 'transparent' : 'action.hover',
                                transition: 'all 0.2s ease',
                                '&:hover': {
                                    backgroundColor: 'action.selected',
                                },
                            }}
                        >
                            <CardContent sx={{ p: 3 }}>
                                <Stack direction="row" spacing={2} alignItems="flex-start">
                                    <Box sx={{ mt: 0.5 }}>
                                        {getNotificationIcon(notification.type)}
                                    </Box>

                                    <Box sx={{ flexGrow: 1 }}>
                                        <Typography
                                            variant="body1"
                                            sx={{
                                                fontWeight: notification.seen ? 'normal' : 'bold',
                                                mb: 1,
                                            }}
                                        >
                                            {notification.message}
                                        </Typography>

                                        <Stack direction="row" spacing={1} alignItems="center" mb={1}>
                                            {notification.flightNumber && (
                                                <Chip
                                                    label={`Flight ${notification.flightNumber}`}
                                                    size="small"
                                                    variant="outlined"
                                                    color="primary"
                                                />
                                            )}
                                            <Chip
                                                label={notification.type.replace(/_/g, ' ')}
                                                size="small"
                                                variant="filled"
                                                color="default"
                                            />
                                        </Stack>

                                        <Box display="flex" alignItems="center" color="text.secondary">
                                            <Schedule sx={{ fontSize: 14, mr: 0.5 }} />
                                            <Typography variant="caption">
                                                {formatTime(notification.createdAt)} • {formatRelativeTime(notification.createdAt)}
                                            </Typography>
                                        </Box>
                                    </Box>

                                    <Stack direction="row" spacing={1}>
                                        {!notification.seen && (
                                            <IconButton
                                                size="small"
                                                onClick={() => handleMarkAsSeen(notification.id)}
                                                title="Mark as read"
                                            >
                                                <MarkAsUnread />
                                            </IconButton>
                                        )}
                                        {!notification.seen && (
                                            <Box
                                                sx={{
                                                    width: 8,
                                                    height: 8,
                                                    backgroundColor: 'primary.main',
                                                    borderRadius: '50%',
                                                    mt: 1,
                                                }}
                                            />
                                        )}
                                    </Stack>
                                </Stack>
                            </CardContent>
                        </Card>
                    </React.Fragment>
                ))}
            </List>
        );
    };

    return (
        <Container maxWidth="lg" sx={{ py: 4, mt: 8 }}>
            {/* Header */}
            <Box mb={4}>
                <Stack
                    direction={{ xs: 'column', sm: 'row' }}
                    justifyContent="space-between"
                    alignItems={{ xs: 'flex-start', sm: 'center' }}
                    spacing={2}
                >
                    <Typography variant="h4" component="h1" color="primary" fontWeight="bold">
                        Notifications
                    </Typography>

                    <Stack direction="row" spacing={1}>
                        <Button
                            variant="outlined"
                            startIcon={<Refresh />}
                            onClick={handleRefresh}
                            disabled={isLoading}
                        >
                            Refresh
                        </Button>
                        {unseenCount > 0 && (
                            <Button
                                variant="contained"
                                startIcon={<CheckCircle />}
                                onClick={handleMarkAllAsSeen}
                                disabled={isLoading}
                            >
                                Mark All Read
                            </Button>
                        )}
                    </Stack>
                </Stack>
            </Box>

            {/* Error Alert */}
            {error && (
                <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
                    {error}
                </Alert>
            )}

            {/* Loading State */}
            {isLoading && (
                <Box display="flex" justifyContent="center" py={8}>
                    <CircularProgress size={60} />
                </Box>
            )}

            {/* Notifications Content */}
            {!isLoading && (
                <Box>
                    {/* Status Tabs */}
                    <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
                        <Tabs
                            value={activeTab}
                            onChange={handleTabChange}
                            variant="scrollable"
                            scrollButtons="auto"
                        >
                            {tabs.map((tab, index) => (
                                <Tab
                                    key={index}
                                    label={tab.label}
                                    sx={{ fontWeight: 600 }}
                                />
                            ))}
                        </Tabs>
                    </Box>

                    {/* Tab Panels */}
                    {tabs.map((tab, index) => (
                        <TabPanel key={index} value={activeTab} index={index}>
                            {renderNotificationList(filterNotificationsByStatus(tab.status))}
                        </TabPanel>
                    ))}
                </Box>
            )}
        </Container>
    );
};