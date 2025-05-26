import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
    Container,
    Typography,
    Box,
    Alert,
    CircularProgress,
    Button,
    Stack,
} from '@mui/material';
import { Search, Refresh } from '@mui/icons-material';
import { FlightSearchForm } from '../../components/flights/FlightSearchForm';
import { FlightCard } from '../../components/flights/FlightCard';
import { Flight, FlightSearchParams } from '../../types/flight';
import { flightService } from '../../services/flightService';

export const FlightsPage: React.FC = () => {
    const [flights, setFlights] = useState<Flight[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [searchParams, setSearchParams] = useState<FlightSearchParams | null>(null);

    const location = useLocation();
    const navigate = useNavigate();

    // Get initial search params from navigation state
    useEffect(() => {
        const stateParams = location.state?.searchParams;
        if (stateParams) {
            setSearchParams(stateParams);
            handleSearch(stateParams);
        } else {
            // Load all flights if no search params
            loadAllFlights();
        }
    }, [location.state]);

    const loadAllFlights = async () => {
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

    const handleSearch = async (params: FlightSearchParams) => {
        setIsLoading(true);
        setError(null);
        setSearchParams(params);

        try {
            const data = await flightService.searchFlights(params);
            setFlights(data);
        } catch (err: any) {
            setError('Failed to search flights. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleBookFlight = (flight: Flight) => {
        navigate('/booking', { state: { flight } });
    };

    const getResultsText = () => {
        if (!searchParams) {
            return `All Available Flights (${flights.length})`;
        }

        return `Flights from ${searchParams.origin} to ${searchParams.destination} (${flights.length} results)`;
    };

    return (
        <Container maxWidth="lg" sx={{ py: 4, mt: 8 }}>
            {/* Search Form */}
            <Box mb={4}>
                <FlightSearchForm
                    onSearch={handleSearch}
                    initialValues={searchParams || undefined}
                    isLoading={isLoading}
                />
            </Box>

            {/* Results Header */}
            <Box mb={3}>
                <Stack
                    direction={{ xs: 'column', sm: 'row' }}
                    justifyContent="space-between"
                    alignItems={{ xs: 'flex-start', sm: 'center' }}
                    spacing={2}
                >
                    <Typography variant="h4" component="h1" color="primary" fontWeight="bold">
                        {getResultsText()}
                    </Typography>

                    <Stack direction="row" spacing={1}>
                        <Button
                            variant="outlined"
                            startIcon={<Refresh />}
                            onClick={searchParams ? () => handleSearch(searchParams) : loadAllFlights}
                            disabled={isLoading}
                        >
                            Refresh
                        </Button>
                        <Button
                            variant="outlined"
                            startIcon={<Search />}
                            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
                        >
                            New Search
                        </Button>
                    </Stack>
                </Stack>

                {searchParams && (
                    <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                        Departure: {searchParams.departureDate} • Passengers: {searchParams.passengers}
                    </Typography>
                )}
            </Box>

            {/* Error Alert */}
            {error && (
                <Alert severity="error" sx={{ mb: 3 }}>
                    {error}
                </Alert>
            )}

            {/* Loading State */}
            {isLoading && (
                <Box display="flex" justifyContent="center" py={8}>
                    <CircularProgress size={60} />
                </Box>
            )}

            {/* Flight Results */}
            {!isLoading && (
                <Box>
                    {flights.length === 0 ? (
                        <Box textAlign="center" py={8}>
                            <Typography variant="h6" color="text.secondary" gutterBottom>
                                {searchParams ? 'No flights found for your search criteria.' : 'No flights available.'}
                            </Typography>
                            <Typography variant="body2" color="text.secondary" mb={3}>
                                Try adjusting your search parameters or check back later.
                            </Typography>
                            {searchParams && (
                                <Button
                                    variant="contained"
                                    onClick={() => {
                                        setSearchParams(null);
                                        loadAllFlights();
                                    }}
                                >
                                    View All Flights
                                </Button>
                            )}
                        </Box>
                    ) : (
                        <Stack spacing={2}>
                            {flights.map((flight) => (
                                <FlightCard
                                    key={flight.id}
                                    flight={flight}
                                    onBook={handleBookFlight}
                                />
                            ))}
                        </Stack>
                    )}
                </Box>
            )}
        </Container>
    );
};