import React, { useState } from 'react';
import {
    Box,
    Card,
    CardContent,
    TextField,
    Button,
    Grid,
    Typography,
    InputAdornment,
    Paper,
    Backdrop,
    IconButton,
} from '@mui/material';
import {
    Search,
    LocationOn,
    CalendarToday,
    Person,
    Close,
    FlightTakeoff,
    FlightLand,
} from '@mui/icons-material';
import { DatePicker, LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import dayjs, { Dayjs } from 'dayjs';
import { FlightSearchParams } from '../../types/flight';

interface FlightSearchFormProps {
    onSearch: (params: FlightSearchParams) => void;
    isModal?: boolean;
    onClose?: () => void;
    initialValues?: Partial<FlightSearchParams>;
    isLoading?: boolean;
}

export const FlightSearchForm: React.FC<FlightSearchFormProps> = ({
                                                                      onSearch,
                                                                      isModal = false,
                                                                      onClose,
                                                                      initialValues,
                                                                      isLoading = false,
                                                                  }) => {
    const [searchParams, setSearchParams] = useState<FlightSearchParams>({
        origin: initialValues?.origin || '',
        destination: initialValues?.destination || '',
        departureDate: initialValues?.departureDate || dayjs().format('YYYY-MM-DD'),
        passengers: initialValues?.passengers || 1,
    });

    const [departureDate, setDepartureDate] = useState<Dayjs | null>(
        dayjs(searchParams.departureDate)
    );

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const params = {
            ...searchParams,
            departureDate: departureDate?.format('YYYY-MM-DD') || searchParams.departureDate,
        };
        onSearch(params);
    };

    const handleInputChange = (field: keyof FlightSearchParams, value: string | number) => {
        setSearchParams(prev => ({
            ...prev,
            [field]: value,
        }));
    };

    const swapLocations = () => {
        setSearchParams(prev => ({
            ...prev,
            origin: prev.destination,
            destination: prev.origin,
        }));
    };

    const FormContent = (
        <Box component="form" onSubmit={handleSubmit} sx={{ width: '100%' }}>
            <Typography variant="h5" component="h2" gutterBottom color="primary" fontWeight="bold">
                Search Flights
            </Typography>
            <Typography variant="body2" color="text.secondary" mb={3}>
                Find the perfect flight for your journey
            </Typography>

            <Grid container spacing={3}>
                {/* Origin and Destination */}
                <Grid item xs={12} md={6}>
                    <TextField
                        fullWidth
                        label="From"
                        value={searchParams.origin}
                        onChange={(e) => handleInputChange('origin', e.target.value)}
                        required
                        InputProps={{
                            startAdornment: (
                                <InputAdornment position="start">
                                    <FlightTakeoff color="action" />
                                </InputAdornment>
                            ),
                        }}
                        placeholder="Enter departure city"
                    />
                </Grid>

                <Grid item xs={12} md={6}>
                    <Box position="relative">
                        <TextField
                            fullWidth
                            label="To"
                            value={searchParams.destination}
                            onChange={(e) => handleInputChange('destination', e.target.value)}
                            required
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <FlightLand color="action" />
                                    </InputAdornment>
                                ),
                            }}
                            placeholder="Enter destination city"
                        />
                        <IconButton
                            size="small"
                            onClick={swapLocations}
                            sx={{
                                position: 'absolute',
                                right: -12,
                                top: '50%',
                                transform: 'translateY(-50%)',
                                backgroundColor: 'primary.main',
                                color: 'white',
                                '&:hover': {
                                    backgroundColor: 'primary.dark',
                                },
                                zIndex: 1,
                            }}
                        >
                            ⇄
                        </IconButton>
                    </Box>
                </Grid>

                {/* Date and Passengers */}
                <Grid item xs={12} md={6}>
                    <LocalizationProvider dateAdapter={AdapterDayjs}>
                        <DatePicker
                            label="Departure Date"
                            value={departureDate}
                            onChange={(newValue) => setDepartureDate(newValue)}
                            minDate={dayjs()}
                            slotProps={{
                                textField: {
                                    fullWidth: true,
                                    required: true,
                                    InputProps: {
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <CalendarToday color="action" />
                                            </InputAdornment>
                                        ),
                                    },
                                },
                            }}
                        />
                    </LocalizationProvider>
                </Grid>

                <Grid item xs={12} md={6}>
                    <TextField
                        fullWidth
                        type="number"
                        label="Passengers"
                        value={searchParams.passengers}
                        onChange={(e) => handleInputChange('passengers', parseInt(e.target.value) || 1)}
                        inputProps={{ min: 1, max: 9 }}
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

                {/* Search Button */}
                <Grid item xs={12}>
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        size="large"
                        startIcon={<Search />}
                        disabled={isLoading}
                        sx={{
                            py: 2,
                            fontSize: '1.1rem',
                            fontWeight: 'bold',
                            borderRadius: 2,
                        }}
                    >
                        {isLoading ? 'Searching...' : 'Search Flights'}
                    </Button>
                </Grid>
            </Grid>
        </Box>
    );

    if (isModal) {
        return (
            <Backdrop open sx={{ zIndex: 1300, p: 2 }}>
                <Paper
                    elevation={8}
                    sx={{
                        p: 4,
                        maxWidth: 600,
                        width: '100%',
                        maxHeight: '90vh',
                        overflow: 'auto',
                        borderRadius: 3,
                        position: 'relative',
                    }}
                >
                    {onClose && (
                        <IconButton
                            onClick={onClose}
                            sx={{
                                position: 'absolute',
                                right: 16,
                                top: 16,
                            }}
                        >
                            <Close />
                        </IconButton>
                    )}
                    {FormContent}
                </Paper>
            </Backdrop>
        );
    }

    return (
        <Card elevation={2}>
            <CardContent sx={{ p: 4 }}>
                {FormContent}
            </CardContent>
        </Card>
    );
};

export default FlightSearchForm;