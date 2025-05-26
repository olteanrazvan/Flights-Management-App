import React, { useState } from 'react';
import {
    Box,
    Card,
    CardContent,
    TextField,
    Button,
    Typography,
    Grid,
    Alert,
    CircularProgress,
    InputAdornment,
    Divider,
    Paper,
} from '@mui/material';
import {
    Person,
    Email,
    AirlineSeatReclineNormal,
    Flight,
    Schedule,
    AttachMoney,
} from '@mui/icons-material';
import { Flight as FlightType } from '../../types/flight';
import { BookingRequest } from '../../types/ticket';
import { ticketService } from '../../services/ticketService';
import { useAuth } from '../../context/AuthContext';
import dayjs from 'dayjs';

interface BookingFormProps {
    flight: FlightType;
    onBookingComplete: (ticketId: number) => void;
    onCancel?: () => void;
}

export const BookingForm: React.FC<BookingFormProps> = ({
                                                            flight,
                                                            onBookingComplete,
                                                            onCancel,
                                                        }) => {
    const { user } = useAuth();
    const [bookingData, setBookingData] = useState<BookingRequest>({
        flightId: flight.id,
        passengerName: user ? `${user.firstName} ${user.lastName}` : '',
        passengerEmail: user?.email || '',
        seatNumber: '',
        price: flight.basePrice,
    });

    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleInputChange = (field: keyof BookingRequest, value: string | number) => {
        setBookingData(prev => ({
            ...prev,
            [field]: value,
        }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError(null);

        try {
            const ticket = await ticketService.createTicket(bookingData);
            onBookingComplete(ticket.id);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Booking failed. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const formatDateTime = (dateString: string) => {
        return dayjs(dateString).format('MMM DD, YYYY • HH:mm');
    };

    const getDuration = () => {
        const departure = dayjs(flight.departureTime);
        const arrival = dayjs(flight.arrivalTime);
        const duration = arrival.diff(departure, 'minute');
        const hours = Math.floor(duration / 60);
        const minutes = duration % 60;
        return `${hours}h ${minutes}m`;
    };

    // Generate available seat numbers (simplified - in real app this would come from backend)
    const generateSeatOptions = () => {
        const seats = [];
        const rows = ['A', 'B', 'C', 'D', 'E', 'F'];
        for (let i = 1; i <= 30; i++) {
            for (const row of rows) {
                seats.push(`${i}${row}`);
            }
        }
        return seats.slice(0, flight.availableSeats);
    };

    const availableSeats = generateSeatOptions();

    return (
        <Box>
            {/* Flight Summary */}
            <Paper elevation={2} sx={{ p: 3, mb: 3, backgroundColor: 'primary.main', color: 'white' }}>
                <Typography variant="h6" gutterBottom display="flex" alignItems="center">
                    <Flight sx={{ mr: 1 }} />
                    Flight {flight.flightNumber}
                </Typography>
                <Grid container spacing={2}>
                    <Grid item xs={12} md={6}>
                        <Typography variant="body1" fontWeight="bold">
                            {flight.origin} → {flight.destination}
                        </Typography>
                        <Typography variant="body2" sx={{ opacity: 0.9 }}>
                            {formatDateTime(flight.departureTime)}
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={3}>
                        <Typography variant="body2" sx={{ opacity: 0.9 }}>
                            Duration: {getDuration()}
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={3}>
                        <Typography variant="h6" fontWeight="bold">
                            ${flight.basePrice}
                        </Typography>
                    </Grid>
                </Grid>
            </Paper>

            {/* Booking Form */}
            <Card>
                <CardContent sx={{ p: 4 }}>
                    <Typography variant="h5" component="h2" gutterBottom color="primary" fontWeight="bold">
                        Passenger Information
                    </Typography>

                    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

                    <Box component="form" onSubmit={handleSubmit}>
                        <Grid container spacing={3}>
                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    label="Passenger Name"
                                    value={bookingData.passengerName}
                                    onChange={(e) => handleInputChange('passengerName', e.target.value)}
                                    required
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <Person color="action" />
                                            </InputAdornment>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    type="email"
                                    label="Email Address"
                                    value={bookingData.passengerEmail}
                                    onChange={(e) => handleInputChange('passengerEmail', e.target.value)}
                                    required
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <Email color="action" />
                                            </InputAdornment>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    select
                                    label="Select Seat"
                                    value={bookingData.seatNumber}
                                    onChange={(e) => handleInputChange('seatNumber', e.target.value)}
                                    required
                                    SelectProps={{
                                        native: true,
                                    }}
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <AirlineSeatReclineNormal color="action" />
                                            </InputAdornment>
                                        ),
                                    }}
                                >
                                    <option value="">Choose a seat</option>
                                    {availableSeats.map((seat) => (
                                        <option key={seat} value={seat}>
                                            Seat {seat}
                                        </option>
                                    ))}
                                </TextField>
                            </Grid>
                        </Grid>

                        <Divider sx={{ my: 3 }} />

                        {/* Price Summary */}
                        <Box sx={{ backgroundColor: 'grey.50', p: 2, borderRadius: 1, mb: 3 }}>
                            <Typography variant="h6" gutterBottom display="flex" alignItems="center">
                                <AttachMoney color="secondary" />
                                Price Summary
                            </Typography>
                            <Box display="flex" justifyContent="space-between" mb={1}>
                                <Typography>Base Price:</Typography>
                                <Typography>${flight.basePrice}</Typography>
                            </Box>
                            <Box display="flex" justifyContent="space-between" mb={1}>
                                <Typography>Taxes & Fees:</Typography>
                                <Typography>$0</Typography>
                            </Box>
                            <Divider sx={{ my: 1 }} />
                            <Box display="flex" justifyContent="space-between">
                                <Typography variant="h6" fontWeight="bold">Total:</Typography>
                                <Typography variant="h6" fontWeight="bold" color="secondary">
                                    ${flight.basePrice}
                                </Typography>
                            </Box>
                        </Box>

                        {/* Action Buttons */}
                        <Grid container spacing={2}>
                            {onCancel && (
                                <Grid item xs={12} md={6}>
                                    <Button
                                        fullWidth
                                        variant="outlined"
                                        size="large"
                                        onClick={onCancel}
                                        disabled={isLoading}
                                    >
                                        Cancel
                                    </Button>
                                </Grid>
                            )}
                            <Grid item xs={12} md={onCancel ? 6 : 12}>
                                <Button
                                    type="submit"
                                    fullWidth
                                    variant="contained"
                                    size="large"
                                    disabled={isLoading || flight.availableSeats === 0}
                                    sx={{ py: 2 }}
                                >
                                    {isLoading ? (
                                        <CircularProgress size={24} />
                                    ) : (
                                        'Complete Booking'
                                    )}
                                </Button>
                            </Grid>
                        </Grid>
                    </Box>
                </CardContent>
            </Card>
        </Box>
    );
};