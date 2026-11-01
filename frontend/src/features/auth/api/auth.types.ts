export interface AuthUser {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string | null;
    roles: string[];
    isActivated: boolean;
    createdAt: string;
    updatedAt: string;
}
export interface LoginRequest {
    email: string;
    password: string;
}
export interface SignupRequest extends LoginRequest {
    firstName: string;
    lastName: string;
    phoneNumber: string | null;
}
