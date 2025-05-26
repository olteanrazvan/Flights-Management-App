import React, { useState } from 'react';
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
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
} from '@mui/material';
import {
    Flight,
    Schedule,
    AirlineSeatReclineNormal,
    Download,
    Cancel,
    CheckCircle,
    Person,
    Email,
    ConfirmationNumber,
} from '@mui/icons-material';
import { Ticket, TicketStatus } from '../../types/ticket';
import { ticketService } from '../../services/ticketService';
import dayjs from 'dayjs';

interface TicketCardProps {
    ticket: Ticket;
    onStatusChange?: (ticketId: number, newStatus: TicketStatus) => void;
    showActions?: boolean;
}

export const TicketCard: React.FC<TicketCardProps> = ({
                                                          ticket,
                                                          onStatusChange,
                                                          showActions = true,
                                                      }) => {
    const [isLoading, setIsLoading] = useState(false);
    const [confirmDialog, setConfirmDialog] = useState<{
        open: boolean;
        action: 'confirm' | 'cancel' | null;
    }>({ open: false, action: null });

    const formatDateTime = (dateString: string) => {
        return dayjs(dateString).format('MMM DD, YYYY • HH:mm');
    };

    const getStatusColor = (status: TicketStatus) => {
        switch (status) {
            case TicketStatus.CONFIRMED:
                return 'success';
            case TicketStatus.CANCELLED:
                return 'error';
            case TicketStatus.RESERVED:
                return 'warning';
            default:
                return 'default';
        }
    };

    const getStatusIcon = (status: TicketStatus) => {
        switch (status) {
            case TicketStatus.CONFIRMED:
                return <CheckCircle />;
            case TicketStatus.CANCELLED:
                return <Cancel />;
            case TicketStatus.RESERVED:
                return <Schedule />;
            default:
                return null;
        }
    };

    const handleDownloadPdf = async () => {
        setIsLoading(true);
        try {
            const pdfBlob = await ticketService.downloadTicketPdf(ticket.id);
            const url = window.URL.createObjectURL(pdfBlob);
            const link = document.createElement('a');
            link.href = url;
            link.download = `ticket_${ticket.ticketNumber}.pdf`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);
        } catch (error) {
            console.error('Failed to download PDF:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleConfirmTicket = async () => {
        setIsLoading(true);
        try {
            await ticketService.confirmTicket(ticket.id);
            onStatusChange?.(ticket.id, TicketStatus.CONFIRMED);
            setConfirmDialog({ open: false, action: null });
        } catch (error) {
            console.error('Failed to confirm ticket:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const handleCancelTicket = async () => {
        setIsLoading(true);
        try {
            await ticketService.cancelTicket(ticket.id);
            onStatusChange?.(ticket.id, TicketStatus.CANCELLED);
            setConfirmDialog({ open: false, action: null });
        } catch (error) {
            console.error('Failed to cancel ticket:', error);
        } finally {
            setIsLoading(false);
        }
    };

    const openConfirmDialog = (action: 'confirm' | 'cancel') => {
        setConfirmDialog({ open: true, action });
    };

    const closeConfirmDialog = () => {
        setConfirmDialog({ open: false, action: null });
    };

    const canConfirm = ticket.status === TicketStatus.RESERVED;
    const canCancel = ticket.status !== TicketStatus.CANCELLED;
    const isPastFlight = dayjs(ticket.departureTime).isBefore(dayjs());

    return (
        <>
            <Card sx={{ mb: 2, position: 'relative', overflow: 'visible' }}>
                {/* Status Chip positioned at top-right */}
                <Chip
                    label={ticket.status}
                    color={getStatusColor(ticket.status)}
                    size="small"
                    sx={{
                        position: 'absolute',
                        top: 16,
                        right: 16,
                        zIndex: 1,
                    }}
                />

                <CardContent sx={{ p: 3, pt: 5 }}>
                    <Grid container spacing={3}>
                        {/* Flight Info */}
                        <Grid item xs={12} md={4}>
                            <Box display="flex" alignItems="center" mb={2}>
                                <Flight color="primary" sx={{ mr: 1 }} />
                                <Typography variant="h6" color="primary" fontWeight="bold">
                                    {ticket.flightNumber}
                                </Typography>
                            </Box>

                            <Typography variant="h5" fontWeight="bold" gutterBottom>
                                {ticket.origin} → {ticket.destination}
                            </Typography>

                            <Box display="flex" alignItems="center" color="text.secondary" mb={1}>
                                <Schedule sx={{ fontSize: 16, mr: 0.5 }} />
                                <Typography variant="body2">
                                    Departure: {formatDateTime(ticket.departureTime || '')}
                                </Typography>
                            </Box>

                            <Typography variant="body2" color="text.secondary">
                                Arrival: {formatDateTime(ticket.arrivalTime || '')}
                            </Typography>
                        </Grid>

                        {/* Passenger Info */}
                        <Grid item xs={12} md={4}>
                            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                                PASSENGER DETAILS
                            </Typography>

                            <Box display="flex" alignItems="center" mb={1}>
                                <Person sx={{ fontSize: 16, mr: 0.5 }} />
                                <Typography variant="body1" fontWeight="medium">
                                    {ticket.passengerName}
                                </Typography>
                            </Box>

                            <Box display="flex" alignItems="center" mb={2}>
                                <Email sx={{ fontSize: 16, mr: 0.5 }} />
                                <Typography variant="body2" color="text.secondary">
                                    {ticket.passengerEmail}
                                </Typography>
                            </Box>

                            <Box display="flex" alignItems="center" mb={1}>
                                <AirlineSeatReclineNormal sx={{ fontSize: 16, mr: 0.5 }} />
                                <Typography variant="body1" fontWeight="medium">
                                    Seat {ticket.seatNumber}
                                </Typography>
                            </Box>

                            <Box display="flex" alignItems="center">
                                <ConfirmationNumber sx={{ fontSize: 16, mr: 0.5 }} />
                                <Typography variant="body2" color="text.secondary">
                                    {ticket.ticketNumber}
                                </Typography>
                            </Box>
                        </Grid>

                        {/* Price and Actions */}
                        <Grid item xs={12} md={4}>
                            <Box textAlign={{ xs: 'left', md: 'right' }}>
                                <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                                    TOTAL PRICE
                                </Typography>
                                <Typography variant="h4" color="secondary" fontWeight="bold" mb={2}>
                                    ${ticket.price}
                                </Typography>

                                <Typography variant="body2" color="text.secondary" mb={3}>
                                    Purchased: {formatDateTime(ticket.purchaseTime)}
                                </Typography>

                                {showActions && (
                                    <Stack spacing={1} direction={{ xs: 'row', md: 'column' }}>
                                        <Button
                                            variant="outlined"
                                            startIcon={<Download />}
                                            onClick={handleDownloadPdf}
                                            disabled={isLoading}
                                            size="small"
                                        >
                                            Download PDF
                                        </Button>

                                        {canConfirm && !isPastFlight && (
                                            <Button
                                                variant="contained"
                                                color="success"
                                                startIcon={<CheckCircle />}
                                                onClick={() => openConfirmDialog('confirm')}
                                                disabled={isLoading}
                                                size="small"
                                            >
                                                Confirm
                                            </Button>
                                        )}

                                        {canCancel && !isPastFlight && (
                                            <Button
                                                variant="outlined"
                                                color="error"
                                                startIcon={<Cancel />}
                                                onClick={() => openConfirmDialog('cancel')}
                                                disabled={isLoading}
                                                size="small"
                                            >
                                                Cancel
                                            </Button>
                                        )}
                                    </Stack>
                                )}
                            </Box>
                        </Grid>
                    </Grid>
                </CardContent>
            </Card>

            {/* Confirmation Dialog */}
            <Dialog open={confirmDialog.open} onClose={closeConfirmDialog}>
                <DialogTitle>
                    {confirmDialog.action === 'confirm' ? 'Confirm Ticket' : 'Cancel Ticket'}
                </DialogTitle>
                <DialogContent>
                    <Typography>
                        {confirmDialog.action === 'confirm'
                            ? `Are you sure you want to confirm this ticket for flight ${ticket.flightNumber}?`
                            : `Are you sure you want to cancel this ticket for flight ${ticket.flightNumber}? This action cannot be undone.`
                        }
                    </Typography>
                </DialogContent>
                <DialogActions>
                    <Button onClick={closeConfirmDialog} disabled={isLoading}>
                        No, Keep Ticket
                    </Button>
                    <Button
                        onClick={confirmDialog.action === 'confirm' ? handleConfirmTicket : handleCancelTicket}
                        color={confirmDialog.action === 'confirm' ? 'success' : 'error'}
                        variant="contained"
                        disabled={isLoading}
                    >
                        {isLoading ? 'Processing...' :
                            confirmDialog.action === 'confirm' ? 'Yes, Confirm' : 'Yes, Cancel'}
                    </Button>
                </DialogActions>
            </Dialog>
        </>
    );
};