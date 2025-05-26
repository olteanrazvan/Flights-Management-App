import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { User, AuthResponse, AuthRequest, RegisterRequest } from '../types/auth';
import { authService } from '../services/authService';

interface AuthContextType {
    user: User | null;
    isLoading: boolean;
    login: (credentials: AuthRequest) => Promise<void>;
    register: (data: RegisterRequest) => Promise<void>;
    logout: () => void;
    isAuthenticated: boolean;
    isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
    children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const login = async (credentials: AuthRequest): Promise<void> => {
        try {
            const response: AuthResponse = await authService.login(credentials);
            const userData: User = {
                id: response.userId,
                email: response.email,
                firstName: response.firstName,
                lastName: response.lastName,
                phoneNumber: '', // Will be fetched from getCurrentUser if needed
                role: response.role,
            };
            setUser(userData);
        } catch (error) {
            throw error;
        }
    };

    const register = async (data: RegisterRequest): Promise<void> => {
        try {
            const response: AuthResponse = await authService.register(data);
            const userData: User = {
                id: response.userId,
                email: response.email,
                firstName: response.firstName,
                lastName: response.lastName,
                phoneNumber: data.phoneNumber,
                role: response.role,
            };
            setUser(userData);
        } catch (error) {
            throw error;
        }
    };

    const logout = (): void => {
        authService.logout();
        setUser(null);
    };

    const checkAuthStatus = async (): Promise<void> => {
        if (authService.isAuthenticated()) {
            try {
                const userData = await authService.getCurrentUser();
                setUser(userData);
            } catch (error) {
                // Token might be invalid, logout
                logout();
            }
        }
        setIsLoading(false);
    };

    useEffect(() => {
        checkAuthStatus();
    }, []);

    const value: AuthContextType = {
        user,
        isLoading,
        login,
        register,
        logout,
        isAuthenticated: !!user,
        isAdmin: user?.role === 'ADMIN',
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = (): AuthContextType => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};