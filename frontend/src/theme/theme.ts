import { createTheme, ThemeOptions } from '@mui/material/styles';

declare module '@mui/material/styles' {
    interface Palette {
        ghost: {
            main: string;
            light: string;
            dark: string;
            contrastText: string;
        };
    }

    interface PaletteOptions {
        ghost?: {
            main: string;
            light: string;
            dark: string;
            contrastText: string;
        };
    }
}

const themeOptions: ThemeOptions = {
    palette: {
        primary: {
            main: '#1976d2', // Aviation blue
            light: '#42a5f5',
            dark: '#1565c0',
            contrastText: '#ffffff',
        },
        secondary: {
            main: '#ff6b35', // Sunset orange
            light: '#ff8a65',
            dark: '#d84315',
            contrastText: '#ffffff',
        },
        ghost: {
            main: '#2c2c54', // Ghost dark blue
            light: '#40407a',
            dark: '#1e1e3f',
            contrastText: '#ffffff',
        },
        background: {
            default: '#f8f9fa',
            paper: '#ffffff',
        },
        text: {
            primary: '#2c2c54',
            secondary: '#6c7293',
        },
    },
    typography: {
        fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
        h1: {
            fontSize: '3.5rem',
            fontWeight: 700,
            letterSpacing: '-0.02em',
            lineHeight: 1.2,
        },
        h2: {
            fontSize: '2.5rem',
            fontWeight: 600,
            lineHeight: 1.3,
        },
        h3: {
            fontSize: '2rem',
            fontWeight: 600,
            lineHeight: 1.4,
        },
        h4: {
            fontSize: '1.5rem',
            fontWeight: 600,
            lineHeight: 1.4,
        },
        h5: {
            fontSize: '1.25rem',
            fontWeight: 600,
            lineHeight: 1.5,
        },
        h6: {
            fontSize: '1rem',
            fontWeight: 600,
            lineHeight: 1.5,
        },
        body1: {
            fontSize: '1rem',
            lineHeight: 1.6,
        },
        body2: {
            fontSize: '0.875rem',
            lineHeight: 1.6,
        },
        button: {
            fontWeight: 600,
            textTransform: 'none',
        },
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: {
                    borderRadius: 12,
                    textTransform: 'none',
                    fontWeight: 600,
                    padding: '10px 24px',
                    boxShadow: 'none',
                    '&:hover': {
                        boxShadow: '0 4px 12px rgba(25, 118, 210, 0.3)',
                    },
                },
                contained: {
                    '&:hover': {
                        boxShadow: '0 6px 20px rgba(25, 118, 210, 0.4)',
                    },
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    borderRadius: 16,
                    boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
                    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                    '&:hover': {
                        transform: 'translateY(-4px)',
                        boxShadow: '0 12px 40px rgba(0,0,0,0.12)',
                    },
                },
            },
        },
        MuiTextField: {
            styleOverrides: {
                root: {
                    '& .MuiOutlinedInput-root': {
                        borderRadius: 12,
                        '&:hover .MuiOutlinedInput-notchedOutline': {
                            borderColor: '#1976d2',
                        },
                    },
                },
            },
        },
        MuiAppBar: {
            styleOverrides: {
                root: {
                    backgroundColor: 'rgba(44, 44, 84, 0.95)',
                    backdropFilter: 'blur(20px)',
                    boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
                },
            },
        },
        MuiChip: {
            styleOverrides: {
                root: {
                    borderRadius: 8,
                    fontWeight: 500,
                },
            },
        },
        MuiPaper: {
            styleOverrides: {
                root: {
                    backgroundImage: 'none',
                },
                elevation1: {
                    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                },
                elevation4: {
                    boxShadow: '0 8px 32px rgba(0,0,0,0.12)',
                },
            },
        },
    },
    shape: {
        borderRadius: 12,
    },
    spacing: 8,
};

export const theme = createTheme(themeOptions);