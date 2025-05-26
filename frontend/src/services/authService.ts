import api from './api';
import { AuthRequest, AuthResponse, RegisterRequest, User } from '../types/auth';

export const authService = {
    async login(credentials: AuthRequest): Promise<AuthResponse> {
        const response = await api.post<AuthResponse>('/auth/authenticate', credentials);
        const { token, refreshToken } = response.data;

        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);

        return response.data;
    },

    async register(data: RegisterRequest): Promise<AuthResponse> {
        const response = await api.post<AuthResponse>('/auth/register', data);

        const { token, refreshToken } = response.data;

        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);

        return response.data;
    },

    async getCurrentUser(): Promise<User> {
        const response = await api.get<User>('/users/me');
        return response.data;
    },

    async changePassword(userId: number, currentPassword: string, newPassword: string): Promise<void> {
        await api.post(`/users/${userId}/change-password`, {
            currentPassword,
            newPassword,
        });
    },

    logout(): void {
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
    },

    isAuthenticated(): boolean {
        return !!localStorage.getItem('token');
    },
};