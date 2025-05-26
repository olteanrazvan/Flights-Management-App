import React, { useState, useEffect } from 'react';
import {
    Container,
    Typography,
    Box,
    Alert,
    CircularProgress,
    Tabs,
    Tab,
    Stack,
    Button,
} from '@mui/material';
import { ConfirmationNumber, Refresh, Search } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { TicketCard } from '../../components/tickets/TicketCard';
import { Ticket, TicketStatus } from '../../types/ticket';
import { ticketService } from '../../services/ticketService';

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
    return (
        <div role="tabpanel" hidden={value !== index}>
            {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
        </div>
    );
};

export const MyTicketsPage: React.FC = () => {
    const [tickets, setTickets] = useState<Ticket[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [activeTab, setActiveTab] = useState(0);

    const navigate = useNavigate();

    useEffect(() => {
        loadTickets();
    }, []);

    const loadTickets = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await ticketService.getMyTickets();
            setTickets(data);
        } catch (err: any) {
            setError('Failed to load tickets. Please try again.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleStatusChange = (ticketId: number, newStatus: TicketStatus) => {
        setTickets(prev =>
            prev.map(ticket =>
                ticket.id === ticketId ? { ...ticket, status: newStatus } : ticket
            )
        );
    };

    const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
        setActiveTab(newValue);
    };

    const filterTicketsByStatus = (status?: TicketStatus) => {
        if (!status) return tickets;
        return tickets.filter(ticket => ticket.status === status);
    };

    const getTicketCounts = () => {
        return {
            all: tickets.length,
            reserved: tickets.filter(t => t.status === TicketStatus.RESERVED).length,
            confirmed: tickets.filter(t => t.status === TicketStatus.CONFIRMED).length,
            cancelled: tickets.filter(t => t.status === TicketStatus.CANCELLED).length,
        };
    };

    const counts = getTicketCounts();

    const tabs = [
        { label: `All Tickets (${counts.all})`, status: undefined },
        { label: `Reserved (${counts.reserved})`, status: TicketStatus.RESERVED },
        { label: `Confirmed (${counts.confirmed})`, status: TicketStatus.CONFIRMED },
        { label: `Cancelled (${counts.cancelled})`, status: TicketStatus.CANCELLED },
    ];

    const renderTicketList = (filteredTickets: Ticket[]) => {
        if (filteredTickets.length === 0) {
            return (
                <Box textAlign="center" py={8}>
                    <ConfirmationNumber sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
                    <Typography variant="h6" color="text.secondary" gutterBottom>
                        No tickets found
                    </Typography>
                    <Typography variant="body2" color="text.secondary" mb={3}>
                        {activeTab === 0
                            ? "You haven't booked any flights yet."
                            : `No tickets with ${tabs[activeTab].status?.toLowerCase()} status.`
                        }
                    </Typography>
                    {activeTab === 0 && (
                        <Button
                            variant="contained"
                            startIcon={<Search />}
                            onClick={() => navigate('/flights')}
                        >
                            Book Your First Flight
                        </Button>
                    )}
                </Box>
            );
        }

        return (
            <Stack spacing={3}>
                {filteredTickets.map((ticket) => (
                    <TicketCard
                        key={ticket.id}
                        ticket={ticket}
                        onStatusChange={handleStatusChange}
                    />
                ))}
            </Stack>
        );
    };

    return (
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
                        My Tickets
                    </Typography>

                    <Stack direction="row" spacing={1}>
                        <Button
                            variant="outlined"
                            startIcon={<Refresh />}
                            onClick={loadTickets}
                            disabled={isLoading}
                        >
                            Refresh
                        </Button>
                        <Button
                            variant="contained"
                            startIcon={<Search />}
                            onClick={() => navigate('/flights')}
                        >
                            Book New Flight
                        </Button>
                    </Stack>
                </Stack>
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

            {/* Tickets Content */}
            {!isLoading && (
                <Box>
                    {/* Status Tabs */}
                    <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
                        <Tabs
                            value={activeTab}
                            onChange={handleTabChange}
                            variant="scrollable"
                            scrollButtons="auto"
                        >
                            {tabs.map((tab, index) => (
                                <Tab
                                    key={index}
                                    label={tab.label}
                                    sx={{ fontWeight: 600 }}
                                />
                            ))}
                        </Tabs>
                    </Box>

                    {/* Tab Panels */}
                    {tabs.map((tab, index) => (
                        <TabPanel key={index} value={activeTab} index={index}>
                            {renderTicketList(filterTicketsByStatus(tab.status))}
                        </TabPanel>
                    ))}
                </Box>
            )}
        </Container>
    );
};