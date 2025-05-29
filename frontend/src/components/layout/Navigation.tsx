import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
    AppBar,
    Toolbar,
    Typography,
    Button,
    IconButton,
    Menu,
    MenuItem,
    Avatar,
    Badge,
    Box,
    Divider,
    Popover,
} from '@mui/material';
import {
    Flight,
    AccountCircle,
    Notifications,
    Dashboard,
    ConfirmationNumber,
    AdminPanelSettings,
    ExitToApp,
    Home,
    Search,
} from '@mui/icons-material';
import { useAuth } from '../../context/AuthContext';
import { useNotifications } from '../../context/NotificationContext';
import { NotificationDropdown } from '../notifications/NotificationDropdown';

export const Navigation: React.FC = () => {
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const [notificationAnchor, setNotificationAnchor] = useState<null | HTMLElement>(null);

    const { user, logout, isAuthenticated } = useAuth();
    const { notifications, unseenCount } = useNotifications();
    const navigate = useNavigate();
    const location = useLocation();

    const handleUserMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };

    const handleUserMenuClose = () => {
        setAnchorEl(null);
    };

    const handleNotificationMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
        setNotificationAnchor(event.currentTarget);
    };

    const handleNotificationMenuClose = () => {
        setNotificationAnchor(null);
    };

    const handleNavigation = (path: string) => {
        navigate(path);
        handleUserMenuClose();
    };

    const handleLogout = () => {
        logout();
        handleUserMenuClose();
        navigate('/');
    };

    const handleViewAllNotifications = () => {
        navigate('/notifications');
    };

    const isActive = (path: string) => location.pathname === path;

    return (
        <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
            <Toolbar>
                <IconButton
                    color="inherit"
                    onClick={() => navigate('/')}
                    sx={{ mr: 2 }}
                >
                    <Flight />
                </IconButton>

                <Typography
                    variant="h6"
                    component="div"
                    sx={{
                        flexGrow: 1,
                        cursor: 'pointer',
                        fontWeight: 700,
                        letterSpacing: '0.5px'
                    }}
                    onClick={() => navigate('/')}
                >
                    GhostFlights
                </Typography>

                {isAuthenticated && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Button
                            color="inherit"
                            startIcon={<Home />}
                            onClick={() => navigate('/dashboard')}
                            sx={{
                                mx: 1,
                                backgroundColor: isActive('/dashboard') ? 'rgba(255,255,255,0.1)' : 'transparent'
                            }}
                        >
                            Dashboard
                        </Button>

                        <Button
                            color="inherit"
                            startIcon={<Search />}
                            onClick={() => navigate('/flights')}
                            sx={{
                                mx: 1,
                                backgroundColor: isActive('/flights') ? 'rgba(255,255,255,0.1)' : 'transparent'
                            }}
                        >
                            Flights
                        </Button>

                        <Button
                            color="inherit"
                            startIcon={<ConfirmationNumber />}
                            onClick={() => navigate('/my-tickets')}
                            sx={{
                                mx: 1,
                                backgroundColor: isActive('/my-tickets') ? 'rgba(255,255,255,0.1)' : 'transparent'
                            }}
                        >
                            My Tickets
                        </Button>

                        {/* Notification Bell */}
                        <IconButton
                            color="inherit"
                            onClick={handleNotificationMenuOpen}
                            sx={{ mx: 1 }}
                        >
                            <Badge badgeContent={unseenCount} color="secondary">
                                <Notifications />
                            </Badge>
                        </IconButton>

                        {/* User Menu */}
                        <Button
                            color="inherit"
                            startIcon={<AccountCircle />}
                            onClick={handleUserMenuOpen}
                            sx={{ ml: 1 }}
                        >
                            {user?.firstName} {user?.lastName}
                            {user?.role === 'ADMIN' && <AdminPanelSettings sx={{ ml: 1 }} />}
                        </Button>
                    </Box>
                )}

                {!isAuthenticated && (
                    <Box>
                        <Button color="inherit" onClick={() => navigate('/login')}>
                            Login
                        </Button>
                        <Button
                            variant="outlined"
                            color="inherit"
                            onClick={() => navigate('/register')}
                            sx={{ ml: 1 }}
                        >
                            Sign Up
                        </Button>
                    </Box>
                )}

                {/* User Menu Dropdown */}
                <Menu
                    anchorEl={anchorEl}
                    open={Boolean(anchorEl)}
                    onClose={handleUserMenuClose}
                    transformOrigin={{ horizontal: 'right', vertical: 'top' }}
                    anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                >
                    <MenuItem onClick={() => handleNavigation('/dashboard')}>
                        <Dashboard sx={{ mr: 1 }} />
                        Dashboard
                    </MenuItem>
                    <MenuItem onClick={() => handleNavigation('/my-tickets')}>
                        <ConfirmationNumber sx={{ mr: 1 }} />
                        My Tickets
                    </MenuItem>
                    {user?.role === 'ADMIN' && (
                        <>
                            <Divider />
                            <MenuItem onClick={() => handleNavigation('/admin/flights')}>
                                <AdminPanelSettings sx={{ mr: 1 }} />
                                Manage Flights
                            </MenuItem>
                            <MenuItem onClick={() => handleNavigation('/admin/dashboard')}>
                                <Dashboard sx={{ mr: 1 }} />
                                Admin Dashboard
                            </MenuItem>
                        </>
                    )}
                    <Divider />
                    <MenuItem onClick={handleLogout}>
                        <ExitToApp sx={{ mr: 1 }} />
                        Logout
                    </MenuItem>
                </Menu>

                {/* Notification Popover */}
                <Popover
                    open={Boolean(notificationAnchor)}
                    anchorEl={notificationAnchor}
                    onClose={handleNotificationMenuClose}
                    anchorOrigin={{
                        vertical: 'bottom',
                        horizontal: 'right',
                    }}
                    transformOrigin={{
                        vertical: 'top',
                        horizontal: 'right',
                    }}
                    PaperProps={{
                        sx: { mt: 1 }
                    }}
                >
                    <NotificationDropdown
                        notifications={notifications}
                        onViewAll={handleViewAllNotifications}
                        onClose={handleNotificationMenuClose}
                    />
                </Popover>
            </Toolbar>
        </AppBar>
    );
};

export default Navigation;