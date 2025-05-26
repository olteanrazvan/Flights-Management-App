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
    Avatar,
    Chip,
} from '@mui/material';
import {
    Flight,
    ConfirmationNumber,
    People,
    AdminPanelSettings,
    Add,
    ViewList,
    TrendingUp,
    Schedule,
} from '@mui/icons-material';
import { useAuth } from '../../context/AuthContext';
import { flightService } from '../../services/flightService';
import { Flight as FlightType } from '../../types/flight';
import dayjs from 'dayjs';

export const AdminDashboard: React.FC = () => {
    const [flights, setFlights] = useState<FlightType[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const { user } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        loadFlights();
    }, []);

    const loadFlights = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await flightService.getAllFlights();
            setFlights(data);
        } catch (err: any) {
            setError('Failed to load flights data.');
        } finally {
            setIsLoading(false);
        }
    };

    const getStats = () => {
        const now = dayjs();
        const upcomingFlights = flights.filter(f => dayjs(f.departureTime).isAfter(now));
        const totalSeats = flights.reduce((sum, f) => sum + f.totalSeats, 0);
        const availableSeats = flights.reduce((sum, f) => sum + f.availableSeats, 0);
        const occupancyRate = totalSeats > 0 ? ((totalSeats - availableSeats) / totalSeats * 100) : 0;

        return {
            totalFlights: flights.length,
            upcomingFlights: upcomingFlights.length,
            totalSeats,
            availableSeats,
            occupancyRate: Math.round(occupancyRate),
        };
    };

    const getRecentFlights = () => {
        return flights
            .sort((a, b) => dayjs(b.departureTime).valueOf() - dayjs(a.departureTime).valueOf())
            .slice(0, 5);
    };

    const stats = getStats();
    const recentFlights = getRecentFlights();

    const formatDateTime = (dateString: string) => {
        return dayjs(dateString).format('MMM DD, YYYY • HH:mm');
    };

    const getAvailabilityColor = (flight: FlightType) => {
        const percentage = (flight.availableSeats / flight.totalSeats) * 100;
        if (percentage > 50) return 'success';
        if (percentage > 20) return 'warning';
        return 'error';
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
                            bgcolor: 'secondary.main',
                            mr: 2
                        }}
                    >
                        <AdminPanelSettings fontSize="large" />
                    </Avatar>
                    <Box>
                        <Typography variant="h4" component="h1" color="primary" fontWeight="bold">
                            Admin Dashboard
                        </Typography>
                        <Typography variant="body1" color="text.secondary">
                            Welcome, {user?.firstName}! Manage your flight operations
                        </Typography>
                    </Box>
                </Box>
            </Box>

            {/* Stats Cards */}
            <Grid container spacing={3} mb={4}>
                <Grid item xs={12} sm={6} md={3}>
                    <Card sx={{ height: '100%' }}>
                        <CardContent sx={{ textAlign: 'center', p: 3 }}>
                            <Flight
                                sx={{ fontSize: 40, color: 'primary.main', mb: 1 }}
                            />
                            <Typography variant="h4" fontWeight="bold" gutterBottom>
                                {stats.totalFlights}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Total Flights
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <Card sx={{ height: '100%' }}>
                        <CardContent sx={{ textAlign: 'center', p: 3 }}>
                            <Schedule
                                sx={{ fontSize: 40, color: 'success.main', mb: 1 }}
                            />
                            <Typography variant="h4" fontWeight="bold" gutterBottom>
                                {stats.upcomingFlights}
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
                            <People
                                sx={{ fontSize: 40, color: 'warning.main', mb: 1 }}
                            />
                            <Typography variant="h4" fontWeight="bold" gutterBottom>
                                {stats.availableSeats}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Available Seats
                            </Typography>
                        </CardContent>
                    </Card>
                </Grid>

                <Grid item xs={12} sm={6} md={3}>
                    <Card sx={{ height: '100%' }}>
                        <CardContent sx={{ textAlign: 'center', p: 3 }}>
                            <TrendingUp
                                sx={{ fontSize: 40, color: 'secondary.main', mb: 1 }}
                            />
                            <Typography variant="h4" fontWeight="bold" gutterBottom>
                                {stats.occupancyRate}%
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                                Occupancy Rate
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
                            startIcon={<Add />}
                            onClick={() => navigate('/admin/flights')}
                            size="large"
                        >
                            Add New Flight
                        </Button>
                        <Button
                            variant="outlined"
                            startIcon={<ViewList />}
                            onClick={() => navigate('/admin/flights')}
                            size="large"
                        >
                            Manage Flights
                        </Button>
                        <Button
                            variant="outlined"
                            startIcon={<ConfirmationNumber />}
                            onClick={() => navigate('/admin/bookings')}
                            size="large"
                        >
                            View Bookings
                        </Button>
                    </Stack>
                </CardContent>
            </Card>

            {/* Recent Flights */}
            <Card>
                <CardContent sx={{ p: 3 }}>
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
                        <Typography variant="h6" fontWeight="bold">
                            Flight Overview
                        </Typography>
                        <Button
                            variant="text"
                            onClick={() => navigate('/admin/flights')}
                            endIcon={<Flight />}
                        >
                            Manage All
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
                    ) : flights.length === 0 ? (
                        <Box textAlign="center" py={4}>
                            <Flight sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
                            <Typography variant="h6" color="text.secondary" gutterBottom>
                                No flights configured
                            </Typography>
                            <Typography variant="body2" color="text.secondary" mb={3}>
                                Add your first flight to get started!
                            </Typography>
                            <Button
                                variant="contained"
                                startIcon={<Add />}
                                onClick={() => navigate('/admin/flights')}
                            >
                                Add Flight
                            </Button>
                        </Box>
                    ) : (
                        <Stack spacing={2}>
                            {recentFlights.map((flight) => (
                                <Card key={flight.id} variant="outlined">
                                    <CardContent sx={{ p: 2 }}>
                                        <Grid container spacing={2} alignItems="center">
                                            <Grid item xs={12} md={3}>
                                                <Box display="flex" alignItems="center" mb={1}>
                                                    <Flight color="primary" sx={{ mr: 1, fontSize: 16 }} />
                                                    <Typography variant="subtitle1" fontWeight="bold">
                                                        {flight.flightNumber}
                                                    </Typography>
                                                </Box>
                                                <Typography variant="body1">
                                                    {flight.origin} → {flight.destination}
                                                </Typography>
                                            </Grid>

                                            <Grid item xs={12} md={4}>
                                                <Typography variant="body2" color="text.secondary">
                                                    {formatDateTime(flight.departureTime)}
                                                </Typography>
                                                <Typography variant="body2" color="text.secondary">
                                                    Duration: {Math.round(dayjs(flight.arrivalTime).diff(dayjs(flight.departureTime), 'minute') / 60)}h
                                                </Typography>
                                            </Grid>

                                            <Grid item xs={12} md={2}>
                                                <Chip
                                                    label={`${flight.availableSeats}/${flight.totalSeats} seats`}
                                                    color={getAvailabilityColor(flight)}
                                                    size="small"
                                                />
                                            </Grid>

                                            <Grid item xs={12} md={3}>
                                                <Box display="flex" justifyContent={{ xs: 'flex-start', md: 'flex-end' }} alignItems="center">
                                                    <Typography variant="h6" color="secondary" fontWeight="bold">
                                                        ${flight.basePrice}
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