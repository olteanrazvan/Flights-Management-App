export interface FormErrors {
    [key: string]: string;
}

export interface SelectOption {
    value: string | number;
    label: string;
}

export interface ConfirmDialogProps {
    open: boolean;
    title: string;
    message: string;
    onConfirm: () => void;
    onCancel: () => void;
    confirmText?: string;
    cancelText?: string;
}

export interface LoadingState {
    isLoading: boolean;
    error: string | null;
}