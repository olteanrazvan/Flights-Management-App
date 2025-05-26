import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
    Container,
    Typography,
    Box,
    Alert,
    Button,
    Card,
    CardContent,
    Stack,
} from '@mui/material';
import { ArrowBack, CheckCircle } from '@mui/icons-material';
import { BookingForm } from '../../components/tickets/BookingForm';
import { Flight } from '../../types/flight';

export const BookingPage: React.FC = () => {
    const [bookingSuccess, setBookingSuccess] = useState(false);
    const [bookedTicketId, setBookedTicketId] = useState<number | null>(null);

    const location = useLocation();
    const navigate = useNavigate();

    const flight = location.state?.flight as Flight;

    useEffect(() => {
        // Redirect if no flight data
        if (!flight) {
            navigate('/flights');
        }
    }, [flight, navigate]);

    const handleBookingComplete = (ticketId: number) => {
        setBookedTicketId(ticketId);
        setBookingSuccess(true);
    };

    const handleBackToFlights = () => {
        navigate('/flights');
    };

    const handleViewTickets = () => {
        navigate('/my-tickets');
    };

    if (!flight) {
        return null; // Will redirect in useEffect
    }

    if (bookingSuccess) {
        return (
            <Container maxWidth="md" sx={{ py: 4, mt: 8 }}>
                <Card sx={{ textAlign: 'center', p: 4 }}>
                    <CardContent>
                        <CheckCircle
                            sx={{
                                fontSize: 80,
                                color: 'success.main',
                                mb: 2
                            }}
                        />
                        <Typography variant="h4" color="success.main" gutterBottom fontWeight="bold">
                            Booking Confirmed!
                        </Typography>
                        <Typography variant="h6" gutterBottom>
                            Your flight has been successfully booked
                        </Typography>
                        <Typography variant="body1" color="text.secondary" mb={4}>
                            Flight {flight.flightNumber} from {flight.origin} to {flight.destination}
                        </Typography>

                        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} justifyContent="center">
                            <Button
                                variant="contained"
                                onClick={handleViewTickets}
                                size="large"
                            >
                                View My Tickets
                            </Button>
                            <Button
                                variant="outlined"
                                onClick={handleBackToFlights}
                                size="large"
                            >
                                Book Another Flight
                            </Button>
                        </Stack>
                    </CardContent>
                </Card>
            </Container>
        );
    }

    return (
        <Container maxWidth="md" sx={{ py: 4, mt: 8 }}>
            {/* Header */}
            <Box mb={4}>
                <Button
                    startIcon={<ArrowBack />}
                    onClick={handleBackToFlights}
                    sx={{ mb: 2 }}
                >
                    Back to Flights
                </Button>

                <Typography variant="h4" component="h1" color="primary" fontWeight="bold">
                    Complete Your Booking
                </Typography>
                <Typography variant="body1" color="text.secondary">
                    You're just one step away from your journey!
                </Typography>
            </Box>

            {/* Flight not available alert */}
            {flight.availableSeats === 0 && (
                <Alert severity="error" sx={{ mb: 3 }}>
                    This flight is no longer available. Please select a different flight.
                </Alert>
            )}

            {/* Booking Form */}
            <BookingForm
                flight={flight}
                onBookingComplete={handleBookingComplete}
                onCancel={handleBackToFlights}
            />
        </Container>
    );
};