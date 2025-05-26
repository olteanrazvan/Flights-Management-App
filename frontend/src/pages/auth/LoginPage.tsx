import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Box, Container, Fade } from '@mui/material';
import { LoginForm } from '../../components/auth/LoginForm';
import { RegisterForm } from '../../components/auth/RegisterForm';
import { useAuth } from '../../context/AuthContext';

export const LoginPage: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { isAuthenticated } = useAuth();

    // Determine if we should show register form based on route
    const [isLogin, setIsLogin] = useState(location.pathname === '/login');

    // Update form type when route changes
    useEffect(() => {
        setIsLogin(location.pathname === '/login');
    }, [location.pathname]);

    // Redirect if already authenticated
    useEffect(() => {
        if (isAuthenticated) {
            const from = (location.state as any)?.from?.pathname || '/dashboard';
            navigate(from, { replace: true });
        }
    }, [isAuthenticated, navigate, location]);

    const switchToRegister = () => {
        setIsLogin(false);
        navigate('/register', { replace: true });
    };

    const switchToLogin = () => {
        setIsLogin(true);
        navigate('/login', { replace: true });
    };

    return (
        <Box
            sx={{
                minHeight: '100vh',
                background: 'linear-gradient(135deg, #2c2c54 0%, #1976d2 50%, #ff6b35 100%)',
                display: 'flex',
                alignItems: 'center',
                position: 'relative',
                '&::before': {
                    content: '""',
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    background: 'rgba(0,0,0,0.2)',
                }
            }}
        >
            <Container maxWidth="sm" sx={{ position: 'relative', zIndex: 1 }}>
                <Box
                    display="flex"
                    justifyContent="center"
                    alignItems="center"
                    minHeight="100vh"
                    py={4}
                >
                    <Fade in timeout={800}>
                        <Box width="100%">
                            {isLogin ? (
                                <LoginForm onSwitchToRegister={switchToRegister} />
                            ) : (
                                <RegisterForm onSwitchToLogin={switchToLogin} />
                            )}
                        </Box>
                    </Fade>
                </Box>
            </Container>
        </Box>
    );
};