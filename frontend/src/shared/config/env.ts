const apiUrl = import.meta.env.VITE_API_URL || "/api";
if (!apiUrl.startsWith("/") && !/^https?:\/\//.test(apiUrl)) {
    throw new Error("VITE_API_URL must be an HTTP(S) URL or an absolute path.");
}
export const env = { apiUrl };
