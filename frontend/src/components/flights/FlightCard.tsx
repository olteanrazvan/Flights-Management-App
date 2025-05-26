import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
    Card,
    CardContent,
    Typography,
    Button,
    Box,
    Chip,
    Grid,
    IconButton,
    Stack,
    Divider,
} from '@mui/material';
import {
    Flight,
    Schedule,
    AirlineSeatReclineNormal,
    AttachMoney,
    Edit,
    Delete,
    TrendingUp,
} from '@mui/icons-material';
import { Flight as FlightType } from '../../types/flight';
import { useAuth } from '../../context/AuthContext';
import dayjs from 'dayjs';

interface FlightCardProps {
    flight: FlightType;
    onEdit?: (flight: FlightType) => void;
    onDelete?: (flightId: number) => void;
    onBook?: (flight: FlightType) => void;
    showActions?: boolean;
}

export const FlightCard: React.FC<FlightCardProps> = ({
                                                          flight,
                                                          onEdit,
                                                          onDelete,
                                                          onBook,
                                                          showActions = true,
                                                      }) => {
    const { isAdmin } = useAuth();
    const navigate = useNavigate();

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

    const getAvailabilityColor = () => {
        const percentage = (flight.availableSeats / flight.totalSeats) * 100;
        if (percentage > 50) return 'success';
        if (percentage > 20) return 'warning';
        return 'error';
    };

    const getAvailabilityText = () => {
        if (flight.availableSeats === 0) return 'Sold Out';
        if (flight.availableSeats <= 10) return 'Few seats left';
        return `${flight.availableSeats} seats available`;
    };

    const handleBookClick = () => {
        if (onBook) {
            onBook(flight);
        } else {
            navigate('/booking', { state: { flight } });
        }
    };

    return (
        <Card
            sx={{
                mb: 2,
                transition: 'all 0.3s ease',
                '&:hover': {
                    transform: 'translateY(-2px)',
                    boxShadow: 4,
                }
            }}
        >
            <CardContent sx={{ p: 3 }}>
                <Grid container spacing={3} alignItems="center">
                    {/* Flight Info */}
                    <Grid item xs={12} md={3}>
                        <Box display="flex" alignItems="center" mb={1}>
                            <Flight color="primary" sx={{ mr: 1 }} />
                            <Typography variant="h6" color="primary" fontWeight="bold">
                                {flight.flightNumber}
                            </Typography>
                        </Box>
                        <Typography variant="body2" color="text.secondary">
                            {getDuration()}
                        </Typography>
                    </Grid>

                    {/* Route */}
                    <Grid item xs={12} md={3}>
                        <Typography variant="h6" fontWeight="bold" gutterBottom>
                            {flight.origin} → {flight.destination}
                        </Typography>
                        <Box display="flex" alignItems="center" color="text.secondary">
                            <Schedule sx={{ fontSize: 16, mr: 0.5 }} />
                            <Typography variant="body2">
                                {formatDateTime(flight.departureTime)}
                            </Typography>
                        </Box>
                        <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                            Arrives: {formatDateTime(flight.arrivalTime)}
                        </Typography>
                    </Grid>

                    {/* Availability */}
                    <Grid item xs={12} md={2}>
                        <Box display="flex" alignItems="center" mb={1}>
                            <AirlineSeatReclineNormal sx={{ fontSize: 16, mr: 0.5 }} />
                            <Chip
                                label={getAvailabilityText()}
                                color={getAvailabilityColor()}
                                size="small"
                                variant="outlined"
                            />
                        </Box>
                        <Typography variant="body2" color="text.secondary">
                            {flight.totalSeats} total seats
                        </Typography>
                    </Grid>

                    {/* Price */}
                    <Grid item xs={12} md={2}>
                        <Box display="flex" alignItems="center" mb={1}>
                            <AttachMoney color="secondary" sx={{ fontSize: 20 }} />
                            <Typography variant="h5" color="secondary" fontWeight="bold">
                                {flight.basePrice}
                            </Typography>
                        </Box>
                        <Typography variant="body2" color="text.secondary">
                            per person
                        </Typography>
                    </Grid>

                    {/* Actions */}
                    <Grid item xs={12} md={2}>
                        {showActions && (
                            <Stack spacing={1}>
                                {isAdmin ? (
                                    <Stack direction="row" spacing={1}>
                                        <IconButton
                                            size="small"
                                            color="primary"
                                            onClick={() => onEdit?.(flight)}
                                        >
                                            <Edit />
                                        </IconButton>
                                        <IconButton
                                            size="small"
                                            color="error"
                                            onClick={() => onDelete?.(flight.id)}
                                        >
                                            <Delete />
                                        </IconButton>
                                    </Stack>
                                ) : (
                                    <Button
                                        variant="contained"
                                        fullWidth
                                        disabled={flight.availableSeats === 0}
                                        onClick={handleBookClick}
                                        startIcon={flight.availableSeats === 0 ? undefined : <TrendingUp />}
                                    >
                                        {flight.availableSeats === 0 ? 'Sold Out' : 'Book Now'}
                                    </Button>
                                )}
                            </Stack>
                        )}
                    </Grid>
                </Grid>
            </CardContent>
        </Card>
    );
};

export default FlightCard;