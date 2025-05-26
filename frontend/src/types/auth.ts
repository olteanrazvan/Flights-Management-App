export interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    phoneNumber: string;
    role: 'CLIENT' | 'ADMIN';
    ticketIds?: number[];
    notificationIds?: number[];
}

export interface AuthRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    phoneNumber: string;
}

export interface AuthResponse {
    token: string;
    refreshToken: string;
    userId: number;
    email: string;
    firstName: string;
    lastName: string;
    role: 'CLIENT' | 'ADMIN';
}