import React from 'react';
import {
    Box,
    Container,
    Typography,
    Button,
    Fade,
    Grid,
    Card,
    CardContent,
    useTheme,
    alpha,
} from '@mui/material';
import { Search, FlightTakeoff, Security, Schedule } from '@mui/icons-material';

interface HeroSectionProps {
    onSearchClick: () => void;
}

export const HeroSection: React.FC<HeroSectionProps> = ({ onSearchClick }) => {
    const theme = useTheme();

    const features = [
        {
            icon: <FlightTakeoff fontSize="large" />,
            title: 'Global Destinations',
            description: 'Fly to over 200+ destinations worldwide',
        },
        {
            icon: <Security fontSize="large" />,
            title: 'Secure Booking',
            description: 'Your data is protected with bank-level security',
        },
        {
            icon: <Schedule fontSize="large" />,
            title: '24/7 Support',
            description: 'Round-the-clock customer service support',
        },
    ];

    return (
        <Box
            sx={{
                position: 'relative',
                minHeight: '100vh',
                background: `linear-gradient(135deg, 
          ${theme.palette.ghost.main} 0%, 
          ${theme.palette.primary.main} 50%, 
          ${theme.palette.secondary.main} 100%)`,
                display: 'flex',
                alignItems: 'center',
                overflow: 'hidden',
                '&::before': {
                    content: '""',
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    background: 'rgba(0,0,0,0.3)',
                    zIndex: 1,
                },
                '&::after': {
                    content: '""',
                    position: 'absolute',
                    top: '20%',
                    right: '-10%',
                    width: '60%',
                    height: '60%',
                    background: `radial-gradient(circle, ${alpha(theme.palette.primary.light, 0.1)} 0%, transparent 70%)`,
                    borderRadius: '50%',
                    zIndex: 0,
                }
            }}
        >
            <Container maxWidth="lg" sx={{ position: 'relative', zIndex: 2 }}>
                <Grid container spacing={6} alignItems="center">
                    <Grid item xs={12} lg={8}>
                        <Fade in timeout={1000}>
                            <Box>
                                <Typography
                                    variant="h1"
                                    color="white"
                                    gutterBottom
                                    sx={{
                                        textShadow: '2px 2px 4px rgba(0,0,0,0.3)',
                                        mb: 3,
                                    }}
                                >
                                    Welcome to
                                    <br />
                                    <Box component="span" sx={{ color: theme.palette.secondary.main }}>
                                        GhostFlights
                                    </Box>
                                </Typography>

                                <Typography
                                    variant="h5"
                                    color="white"
                                    sx={{
                                        mb: 4,
                                        opacity: 0.95,
                                        textShadow: '1px 1px 2px rgba(0,0,0,0.3)',
                                        maxWidth: '600px',
                                        lineHeight: 1.4,
                                    }}
                                >
                                    Discover amazing destinations around the world. Book your perfect flight
                                    with confidence and explore the skies like never before.
                                </Typography>

                                <Box sx={{ mb: 4 }}>
                                    <Button
                                        variant="contained"
                                        size="large"
                                        startIcon={<Search />}
                                        onClick={onSearchClick}
                                        sx={{
                                            py: 2,
                                            px: 4,
                                            fontSize: '1.2rem',
                                            fontWeight: 'bold',
                                            backgroundColor: 'white',
                                            color: theme.palette.primary.main,
                                            borderRadius: 3,
                                            boxShadow: '0 8px 25px rgba(0,0,0,0.2)',
                                            '&:hover': {
                                                backgroundColor: 'rgba(255,255,255,0.95)',
                                                transform: 'translateY(-2px)',
                                                boxShadow: '0 12px 35px rgba(0,0,0,0.3)',
                                            },
                                            transition: 'all 0.3s ease',
                                        }}
                                    >
                                        Search Flights
                                    </Button>
                                </Box>

                                {/* Stats */}
                                <Box
                                    sx={{
                                        display: 'flex',
                                        gap: 4,
                                        flexWrap: 'wrap',
                                        '& > div': {
                                            textAlign: 'center',
                                        }
                                    }}
                                >
                                    <Box>
                                        <Typography variant="h4" color="white" fontWeight="bold">
                                            200+
                                        </Typography>
                                        <Typography variant="body2" color="white" sx={{ opacity: 0.8 }}>
                                            Destinations
                                        </Typography>
                                    </Box>
                                    <Box>
                                        <Typography variant="h4" color="white" fontWeight="bold">
                                            50K+
                                        </Typography>
                                        <Typography variant="body2" color="white" sx={{ opacity: 0.8 }}>
                                            Happy Travelers
                                        </Typography>
                                    </Box>
                                    <Box>
                                        <Typography variant="h4" color="white" fontWeight="bold">
                                            24/7
                                        </Typography>
                                        <Typography variant="body2" color="white" sx={{ opacity: 0.8 }}>
                                            Support
                                        </Typography>
                                    </Box>
                                </Box>
                            </Box>
                        </Fade>
                    </Grid>

                    <Grid item xs={12} lg={4}>
                        <Fade in timeout={1500}>
                            <Box>
                                <Grid container spacing={2}>
                                    {features.map((feature, index) => (
                                        <Grid item xs={12} key={index}>
                                            <Card
                                                sx={{
                                                    backgroundColor: alpha(theme.palette.background.paper, 0.1),
                                                    backdropFilter: 'blur(10px)',
                                                    border: `1px solid ${alpha(theme.palette.common.white, 0.2)}`,
                                                    transition: 'all 0.3s ease',
                                                    '&:hover': {
                                                        backgroundColor: alpha(theme.palette.background.paper, 0.15),
                                                        transform: 'translateY(-4px)',
                                                    }
                                                }}
                                            >
                                                <CardContent sx={{ p: 2 }}>
                                                    <Box display="flex" alignItems="center" gap={2}>
                                                        <Box sx={{ color: theme.palette.secondary.main }}>
                                                            {feature.icon}
                                                        </Box>
                                                        <Box>
                                                            <Typography
                                                                variant="h6"
                                                                color="white"
                                                                fontWeight="bold"
                                                                gutterBottom
                                                            >
                                                                {feature.title}
                                                            </Typography>
                                                            <Typography
                                                                variant="body2"
                                                                color="white"
                                                                sx={{ opacity: 0.9 }}
                                                            >
                                                                {feature.description}
                                                            </Typography>
                                                        </Box>
                                                    </Box>
                                                </CardContent>
                                            </Card>
                                        </Grid>
                                    ))}
                                </Grid>
                            </Box>
                        </Fade>
                    </Grid>
                </Grid>
            </Container>

            {/* Floating Elements */}
            <Box
                sx={{
                    position: 'absolute',
                    top: '10%',
                    left: '5%',
                    opacity: 0.1,
                    transform: 'rotate(-15deg)',
                    zIndex: 1,
                }}
            >
                <FlightTakeoff sx={{ fontSize: 120, color: 'white' }} />
            </Box>

            <Box
                sx={{
                    position: 'absolute',
                    bottom: '15%',
                    right: '10%',
                    opacity: 0.1,
                    transform: 'rotate(25deg)',
                    zIndex: 1,
                }}
            >
                <FlightTakeoff sx={{ fontSize: 80, color: 'white' }} />
            </Box>
        </Box>
    );
};

export default HeroSection;