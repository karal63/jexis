import axios from "axios";

// Vite proxies /api to Spring Boot during development. Set VITE_API_URL when
// the frontend is served separately in another environment.
export const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || "/api",
    withCredentials: true,
    headers: { "Content-Type": "application/json" },
});

export interface ApiError {
    code?: string;
    message?: string;
    errors?: { field: string; message: string }[];
}

export function getApiError(error: unknown): string {
    if (axios.isAxiosError<ApiError>(error)) {
        const data = error.response?.data;
        if (data?.errors?.length) return data.errors.map((item) => item.message).join(" ");
        if (data?.message) return data.message;
    }
    return "Unable to connect. Please try again.";
}
