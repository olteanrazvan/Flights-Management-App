import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Container,
    Typography,
    Box,
    Grid,
    Card,
    CardContent,
    Button,
    Stack,
    CircularProgress,
    Alert,
    Chip,
    Avatar,
} from '@mui/material';
import {
    Flight,
    ConfirmationNumber,
    Search,
    Notifications,
    Person,
    TrendingUp,
    Schedule,
} from '@mui/icons-material';
import { useAuth } from '../../context/AuthContext';
import { useNotifications } from '../../context/NotificationContext';
import { ticketService } from '../../services/ticketService';
import { Ticket, TicketStatus } from '../../types/ticket';
import dayjs from 'dayjs';

export const ClientDashboard: React.FC = () => {
    const [tickets, setTickets] = useState<Ticket[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const { user } = useAuth();
    const { notifications, unseenCount } = useNotifications();
    const navigate = useNavigate();

    useEffect(() => {
        loadRecentTickets();
    }, []);

    const loadRecentTickets = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await ticketService.getMyTickets();
            // Get last 3 tickets sorted by purchase time
            const recentTickets = data
                .sort((a, b) => dayjs(b.purchaseTime).valueOf() - dayjs(a.purchaseTime).valueOf())
                .slice(0, 3);
            setTickets(recentTickets);
        } catch (err: any) {
            setError('Failed to load tickets.');
        } finally {
            setIsLoading(false);
        }
    };

    const getTicketStats = () => {
        return {
            total: tickets.length,
            confirmed: tickets.filter(t => t.status === TicketStatus.CONFIRMED).length,
            reserved: tickets.filter(t => t.status === TicketStatus.RESERVED).length,
        };
    };

    const getUpcomingFlights = () => {
        return tickets.filter(ticket =>
            ticket.departureTime &&
            dayjs(ticket.departureTime).isAfter(dayjs()) &&
            ticket.status !== TicketStatus.CANCELLED
        );
    };

    const stats = getTicketStats();
    const upcomingFlights = getUpcomingFlights();

    const formatDateTime = (dateString: string) => {
        return dayjs(dateString).format('MMM DD, YYYY • HH:mm');
    };

    const getStatusColor = (status: TicketStatus) => {
        switch (status) {
            case TicketStatus.CONFIRMED:
                return 'success';
            case TicketStatus.CANCELLED:
                return 'error';
            case TicketStatus.RESERVED:
                return 'warning';
            default:
                return 'default';
        }
    };

    return (
        <Container maxWidth="lg" sx={{ py: 4, mt: 8 }}>
            {/* Welcome Header */}
            <Box mb={4}>
                <Box display="flex" alignItems="center" mb={2}>
                    <Avatar
                        sx={{
                            width: 60,
                            height: 60,
                            bgcolor: 'primary.main',
                            mr: 2
                        }}
                    >
                        <Person fontSize="large" />
                    </Avatar>
                    <Box>
                        <Typography variant="h4" component="h1" color="primary" fontWeight="bold">
                            Welcome back, {user?.firstName}!
                        </Typography>
                        <Typography variant="body1" color="text.secondary">
                            Here's your flight dashboard
                        </Typography>
                    </Box>
                </Box>
            </Box>

            {/* Stats Cards */}
            <Grid container spacing={3} mb={4}>
                <Grid item xs={12} sm={6} md={3}>
                    <Card sx={{ height: '100%' }}>
                        <CardContent sx={{ textAlign: 'center', p: 3 }}>
                            <ConfirmationNumber
                                sx={{ fontSize: 40, color: 'primary.main', mb: 1 }}
                            />
                            <Typography variant="h4" fontWeight="bold" gutterBottom>
                                {tickets.length}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Total Bookings
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <Card sx={{ height: '100%' }}>
                        <CardContent sx={{ textAlign: 'center', p: 3 }}>
                            <Flight
                                sx={{ fontSize: 40, color: 'success.main', mb: 1 }}
                            />
                            <Typography variant="h4" fontWeight="bold" gutterBottom>
                                {upcomingFlights.length}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Upcoming Flights
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <Card sx={{ height: '100%' }}>
                        <CardContent sx={{ textAlign: 'center', p: 3 }}>
                            <TrendingUp
                                sx={{ fontSize: 40, color: 'warning.main', mb: 1 }}
                            />
                            <Typography variant="h4" fontWeight="bold" gutterBottom>
                                {stats.confirmed}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Confirmed
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <Card sx={{ height: '100%' }}>
                        <CardContent sx={{ textAlign: 'center', p: 3 }}>
                            <Notifications
                                sx={{ fontSize: 40, color: 'secondary.main', mb: 1 }}
                            />
                            <Typography variant="h4" fontWeight="bold" gutterBottom>
                                {unseenCount}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                New Notifications
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>
            </Grid>

            {/* Quick Actions */}
            <Card sx={{ mb: 4 }}>
                <CardContent sx={{ p: 3 }}>
                    <Typography variant="h6" gutterBottom fontWeight="bold">
                        Quick Actions
                    </Typography>
                    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                        <Button
                            variant="contained"
                            startIcon={<Search />}
                            onClick={() => navigate('/flights')}
                            size="large"
                        >
                            Search Flights
                        </Button>
                        <Button
                            variant="outlined"
                            startIcon={<ConfirmationNumber />}
                            onClick={() => navigate('/my-tickets')}
                            size="large"
                        >
                            View All Tickets
                        </Button>
                        <Button
                            variant="outlined"
                            startIcon={<Notifications />}
                            onClick={() => navigate('/notifications')}
                            size="large"
                        >
                            Notifications ({unseenCount})
                        </Button>
                    </Stack>
                </CardContent>
            </Card>

            {/* Recent Tickets */}
            <Card>
                <CardContent sx={{ p: 3 }}>
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                        <Typography variant="h6" fontWeight="bold">
                            Recent Bookings
                        </Typography>
                        <Button
                            variant="text"
                            onClick={() => navigate('/my-tickets')}
                            endIcon={<ConfirmationNumber />}
                        >
                            View All
                        </Button>
                    </Box>

                    {error && (
                        <Alert severity="error" sx={{ mb: 2 }}>
                            {error}
                        </Alert>
                    )}

                    {isLoading ? (
                        <Box display="flex" justifyContent="center" py={4}>
                            <CircularProgress />
                        </Box>
                    ) : tickets.length === 0 ? (
                        <Box textAlign="center" py={4}>
                            <Flight sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
                            <Typography variant="h6" color="text.secondary" gutterBottom>
                                No bookings yet
                            </Typography>
                            <Typography variant="body2" color="text.secondary" mb={3}>
                                Book your first flight to get started!
                            </Typography>
                            <Button
                                variant="contained"
                                startIcon={<Search />}
                                onClick={() => navigate('/flights')}
                            >
                                Search Flights
                            </Button>
                        </Box>
                    ) : (
                        <Stack spacing={2}>
                            {tickets.map((ticket) => (
                                <Card key={ticket.id} variant="outlined">
                                    <CardContent sx={{ p: 2 }}>
                                        <Grid container spacing={2} alignItems="center">
                                            <Grid item xs={12} md={4}>
                                                <Box display="flex" alignItems="center" mb={1}>
                                                    <Flight color="primary" sx={{ mr: 1, fontSize: 16 }} />
                                                    <Typography variant="subtitle1" fontWeight="bold">
                                                        {ticket.flightNumber}
                                                    </Typography>
                                                </Box>
                                                <Typography variant="body1">
                                                    {ticket.origin} → {ticket.destination}
                                                </Typography>
                                            </Grid>

                                            <Grid item xs={12} md={4}>
                                                <Box display="flex" alignItems="center" mb={1}>
                                                    <Schedule sx={{ mr: 1, fontSize: 16, color: 'text.secondary' }} />
                                                    <Typography variant="body2" color="text.secondary">
                                                        {ticket.departureTime && formatDateTime(ticket.departureTime)}
                                                    </Typography>
                                                </Box>
                                                <Typography variant="body2" color="text.secondary">
                                                    Seat {ticket.seatNumber}
                                                </Typography>
                                            </Grid>

                                            <Grid item xs={12} md={4}>
                                                <Box display="flex" justifyContent={{ xs: 'flex-start', md: 'flex-end' }} alignItems="center">
                                                    <Chip
                                                        label={ticket.status}
                                                        color={getStatusColor(ticket.status)}
                                                        size="small"
                                                        sx={{ mr: 2 }}
                                                    />
                                                    <Typography variant="h6" color="secondary" fontWeight="bold">
                                                        ${ticket.price}
                                                    </Typography>
                                                </Box>
                                            </Grid>
                                        </Grid>
                                    </CardContent>
                                </Card>
                            ))}
                        </Stack>
                    )}
                </CardContent>
            </Card>
        </Container>
    );
};