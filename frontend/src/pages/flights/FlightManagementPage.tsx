import React, { useState, useEffect } from 'react';
import {
    Container,
    Typography,
    Box,
    Button,
    Stack,
    Alert,
    CircularProgress,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Grid,
    InputAdornment,
} from '@mui/material';
import {
    Add,
    Refresh,
    FlightTakeoff,
    FlightLand,
    Schedule,
    AttachMoney,
    AirlineSeatReclineNormal,
} from '@mui/icons-material';
import { DateTimePicker, LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs, { Dayjs } from 'dayjs';
import { FlightCard } from '../../components/flights/FlightCard';
import { Flight } from '../../types/flight';
import { flightService } from '../../services/flightService';

interface FlightFormData {
    flightNumber: string;
    origin: string;
    destination: string;
    departureTime: Dayjs | null;
    arrivalTime: Dayjs | null;
    totalSeats: number;
    basePrice: number;
}

const initialFormData: FlightFormData = {
    flightNumber: '',
    origin: '',
    destination: '',
    departureTime: null,
    arrivalTime: null,
    totalSeats: 180,
    basePrice: 299,
};

export const FlightManagementPage: React.FC = () => {
    const [flights, setFlights] = useState<Flight[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [dialogOpen, setDialogOpen] = useState(false);
    const [editingFlight, setEditingFlight] = useState<Flight | null>(null);
    const [formData, setFormData] = useState<FlightFormData>(initialFormData);
    const [formErrors, setFormErrors] = useState<Record<string, string>>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [deleteDialog, setDeleteDialog] = useState<{
        open: boolean;
        flightId: number | null;
    }>({ open: false, flightId: null });

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
            setError('Failed to load flights. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const openAddDialog = () => {
        setEditingFlight(null);
        setFormData(initialFormData);
        setFormErrors({});
        setDialogOpen(true);
    };

    const openEditDialog = (flight: Flight) => {
        setEditingFlight(flight);
        setFormData({
            flightNumber: flight.flightNumber,
            origin: flight.origin,
            destination: flight.destination,
            departureTime: dayjs(flight.departureTime),
            arrivalTime: dayjs(flight.arrivalTime),
            totalSeats: flight.totalSeats,
            basePrice: flight.basePrice,
        });
        setFormErrors({});
        setDialogOpen(true);
    };

    const closeDialog = () => {
        setDialogOpen(false);
        setEditingFlight(null);
        setFormData(initialFormData);
        setFormErrors({});
    };

    const validateForm = (): boolean => {
        const errors: Record<string, string> = {};

        if (!formData.flightNumber.trim()) {
            errors.flightNumber = 'Flight number is required';
        }

        if (!formData.origin.trim()) {
            errors.origin = 'Origin is required';
        }

        if (!formData.destination.trim()) {
            errors.destination = 'Destination is required';
        }

        if (formData.origin === formData.destination) {
            errors.destination = 'Destination must be different from origin';
        }

        if (!formData.departureTime) {
            errors.departureTime = 'Departure time is required';
        }

        if (!formData.arrivalTime) {
            errors.arrivalTime = 'Arrival time is required';
        }

        if (formData.departureTime && formData.arrivalTime) {
            if (formData.arrivalTime.isBefore(formData.departureTime)) {
                errors.arrivalTime = 'Arrival time must be after departure time';
            }
        }

        if (formData.totalSeats < 1) {
            errors.totalSeats = 'Total seats must be at least 1';
        }

        if (formData.basePrice < 0) {
            errors.basePrice = 'Base price cannot be negative';
        }

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleSubmit = async () => {
        if (!validateForm()) return;

        setIsSubmitting(true);
        try {
            const flightData = {
                flightNumber: formData.flightNumber,
                origin: formData.origin,
                destination: formData.destination,
                departureTime: formData.departureTime!.toISOString(),
                arrivalTime: formData.arrivalTime!.toISOString(),
                totalSeats: formData.totalSeats,
                basePrice: formData.basePrice,
                availableSeats: editingFlight ? editingFlight.availableSeats : formData.totalSeats,
            };

            if (editingFlight) {
                await flightService.updateFlight(editingFlight.id, flightData);
            } else {
                await flightService.createFlight(flightData);
            }

            closeDialog();
            loadFlights();
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to save flight. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDelete = async (flightId: number) => {
        setDeleteDialog({ open: true, flightId });
    };

    const confirmDelete = async () => {
        if (!deleteDialog.flightId) return;

        try {
            await flightService.deleteFlight(deleteDialog.flightId);
            setDeleteDialog({ open: false, flightId: null });
            loadFlights();
        } catch (err: any) {
            setError('Failed to delete flight. Please try again.');
            setDeleteDialog({ open: false, flightId: null });
        }
    };

    const handleInputChange = (field: keyof FlightFormData, value: any) => {
        setFormData(prev => ({
            ...prev,
            [field]: value,
        }));

        // Clear error when user starts typing
        if (formErrors[field]) {
            setFormErrors(prev => ({
                ...prev,
                [field]: '',
            }));
        }
    };

    return (
        <LocalizationProvider dateAdapter={AdapterDayjs}>
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
                            Flight Management
                        </Typography>

                        <Stack direction="row" spacing={1}>
                            <Button
                                variant="outlined"
                                startIcon={<Refresh />}
                                onClick={loadFlights}
                                disabled={isLoading}
                            >
                                Refresh
                            </Button>
                            <Button
                                variant="contained"
                                startIcon={<Add />}
                                onClick={openAddDialog}
                            >
                                Add Flight
                            </Button>
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

                {/* Flight List */}
                {!isLoading && (
                    <Box>
                        {flights.length === 0 ? (
                            <Box textAlign="center" py={8}>
                                <FlightTakeoff sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
                                <Typography variant="h6" color="text.secondary" gutterBottom>
                                    No flights configured
                                </Typography>
                                <Typography variant="body2" color="text.secondary" mb={3}>
                                    Add your first flight to get started!
                                </Typography>
                                <Button
                                    variant="contained"
                                    startIcon={<Add />}
                                    onClick={openAddDialog}
                                >
                                    Add Flight
                                </Button>
                            </Box>
                        ) : (
                            <Stack spacing={2}>
                                {flights.map((flight) => (
                                    <FlightCard
                                        key={flight.id}
                                        flight={flight}
                                        onEdit={openEditDialog}
                                        onDelete={handleDelete}
                                    />
                                ))}
                            </Stack>
                        )}
                    </Box>
                )}

                {/* Add/Edit Flight Dialog */}
                <Dialog
                    open={dialogOpen}
                    onClose={closeDialog}
                    maxWidth="md"
                    fullWidth
                >
                    <DialogTitle>
                        {editingFlight ? 'Edit Flight' : 'Add New Flight'}
                    </DialogTitle>
                    <DialogContent>
                        <Grid container spacing={3} sx={{ mt: 1 }}>
                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Flight Number"
                                    value={formData.flightNumber}
                                    onChange={(e) => handleInputChange('flightNumber', e.target.value)}
                                    error={!!formErrors.flightNumber}
                                    helperText={formErrors.flightNumber}
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <FlightTakeoff />
                                            </InputAdornment>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    type="number"
                                    label="Total Seats"
                                    value={formData.totalSeats}
                                    onChange={(e) => handleInputChange('totalSeats', parseInt(e.target.value) || 0)}
                                    error={!!formErrors.totalSeats}
                                    helperText={formErrors.totalSeats}
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <AirlineSeatReclineNormal />
                                            </InputAdornment>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Origin"
                                    value={formData.origin}
                                    onChange={(e) => handleInputChange('origin', e.target.value)}
                                    error={!!formErrors.origin}
                                    helperText={formErrors.origin}
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <FlightTakeoff />
                                            </InputAdornment>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <TextField
                                    fullWidth
                                    label="Destination"
                                    value={formData.destination}
                                    onChange={(e) => handleInputChange('destination', e.target.value)}
                                    error={!!formErrors.destination}
                                    helperText={formErrors.destination}
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <FlightLand />
                                            </InputAdornment>
                                        ),
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <DateTimePicker
                                    label="Departure Time"
                                    value={formData.departureTime}
                                    onChange={(value) => handleInputChange('departureTime', value)}
                                    minDateTime={dayjs()}
                                    slotProps={{
                                        textField: {
                                            fullWidth: true,
                                            error: !!formErrors.departureTime,
                                            helperText: formErrors.departureTime,
                                            InputProps: {
                                                startAdornment: (
                                                    <InputAdornment position="start">
                                                        <Schedule />
                                                    </InputAdornment>
                                                ),
                                            },
                                        },
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12} md={6}>
                                <DateTimePicker
                                    label="Arrival Time"
                                    value={formData.arrivalTime}
                                    onChange={(value) => handleInputChange('arrivalTime', value)}
                                    minDateTime={formData.departureTime || dayjs()}
                                    slotProps={{
                                        textField: {
                                            fullWidth: true,
                                            error: !!formErrors.arrivalTime,
                                            helperText: formErrors.arrivalTime,
                                            InputProps: {
                                                startAdornment: (
                                                    <InputAdornment position="start">
                                                        <Schedule />
                                                    </InputAdornment>
                                                ),
                                            },
                                        },
                                    }}
                                />
                            </Grid>

                            <Grid item xs={12}>
                                <TextField
                                    fullWidth
                                    type="number"
                                    label="Base Price ($)"
                                    value={formData.basePrice}
                                    onChange={(e) => handleInputChange('basePrice', parseFloat(e.target.value) || 0)}
                                    error={!!formErrors.basePrice}
                                    helperText={formErrors.basePrice}
                                    InputProps={{
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <AttachMoney />
                                            </InputAdornment>
                                        ),
                                    }}
                                />
                            </Grid>
                        </Grid>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={closeDialog} disabled={isSubmitting}>
                            Cancel
                        </Button>
                        <Button
                            onClick={handleSubmit}
                            variant="contained"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? 'Saving...' : (editingFlight ? 'Update Flight' : 'Add Flight')}
                        </Button>
                    </DialogActions>
                </Dialog>

                {/* Delete Confirmation Dialog */}
                <Dialog open={deleteDialog.open} onClose={() => setDeleteDialog({ open: false, flightId: null })}>
                    <DialogTitle>Delete Flight</DialogTitle>
                    <DialogContent>
                        <Typography>
                            Are you sure you want to delete this flight? This action cannot be undone.
                        </Typography>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setDeleteDialog({ open: false, flightId: null })}>
                            Cancel
                        </Button>
                        <Button onClick={confirmDelete} color="error" variant="contained">
                            Delete
                        </Button>
                    </DialogActions>
                </Dialog>
            </Container>
        </LocalizationProvider>
    );
};