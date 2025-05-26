import React, { useState } from 'react';
import { Box } from '@mui/material';
import { HeroSection } from '../components/layout/HeroSection';
import { FlightSearchForm } from '../components/flights/FlightSearchForm';
import { FlightSearchParams } from '../types/flight';
import { useNavigate } from 'react-router-dom';

export const HomePage: React.FC = () => {
    const [showSearchModal, setShowSearchModal] = useState(false);
    const navigate = useNavigate();

    const handleSearchFlights = (params: FlightSearchParams) => {
        setShowSearchModal(false);
        navigate('/flights', { state: { searchParams: params } });
    };

    return (
        <Box>
            <HeroSection onSearchClick={() => setShowSearchModal(true)} />

            {showSearchModal && (
                <FlightSearchForm
                    isModal
                    onClose={() => setShowSearchModal(false)}
                    onSearch={handleSearchFlights}
                />
            )}
        </Box>
    );
};