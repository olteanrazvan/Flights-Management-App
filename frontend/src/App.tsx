import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, CssBaseline, Box } from '@mui/material';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';

// Theme
import { theme } from './theme/theme';

// Context Providers
import { AuthProvider } from './context/AuthContext';
import { NotificationProvider } from './context/NotificationContext';

// Components
import { Navigation } from './components/layout/Navigation';
import { AuthGuard } from './components/auth/AuthGuard';

// Pages
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/auth/LoginPage';
import { FlightsPage } from './pages/flights/FlightsPage';
import { MyTicketsPage } from './pages/tickets/MyTicketsPage';
import { BookingPage } from './pages/tickets/BookingPage';
import { ClientDashboard } from './pages/dashboard/ClientDashboard';
import { AdminDashboard } from './pages/dashboard/AdminDashboard';
import { FlightManagementPage } from './pages/flights/FlightManagementPage';

function App() {
    return (
        <ThemeProvider theme={theme}>
            <CssBaseline />
            <LocalizationProvider dateAdapter={AdapterDayjs}>
                <AuthProvider>
                    <Router>
                        <NotificationProvider>
                            <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
                                <Navigation />

                                <Box component="main" sx={{ flexGrow: 1 }}>
                                    <Routes>
                                        {/* Public Routes */}
                                        <Route path="/" element={<HomePage />} />
                                        <Route path="/login" element={<LoginPage key="login" />} />
                                        <Route path="/register" element={<LoginPage key="register" />} />

                                        {/* Protected Routes */}
                                        <Route
                                            path="/dashboard"
                                            element={
                                                <AuthGuard>
                                                    <ClientDashboard />
                                                </AuthGuard>
                                            }
                                        />

                                        <Route
                                            path="/flights"
                                            element={
                                                <AuthGuard>
                                                    <FlightsPage />
                                                </AuthGuard>
                                            }
                                        />

                                        <Route
                                            path="/my-tickets"
                                            element={
                                                <AuthGuard>
                                                    <MyTicketsPage />
                                                </AuthGuard>
                                            }
                                        />

                                        <Route
                                            path="/booking"
                                            element={
                                                <AuthGuard>
                                                    <BookingPage />
                                                </AuthGuard>
                                            }
                                        />

                                        {/* Admin Routes */}
                                        <Route
                                            path="/admin/dashboard"
                                            element={
                                                <AuthGuard requireAdmin>
                                                    <AdminDashboard />
                                                </AuthGuard>
                                            }
                                        />

                                        <Route
                                            path="/admin/flights"
                                            element={
                                                <AuthGuard requireAdmin>
                                                    <FlightManagementPage />
                                                </AuthGuard>
                                            }
                                        />

                                        {/* Catch all - redirect to home */}
                                        <Route path="*" element={<Navigate to="/" replace />} />
                                    </Routes>
                                </Box>
                            </Box>
                        </NotificationProvider>
                    </Router>
                </AuthProvider>
            </LocalizationProvider>
        </ThemeProvider>
    );
}

export default App;