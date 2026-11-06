import { baseApi } from "@/shared/api/baseApi";
import { sessionReceived } from "../model/sessionSlice";
import type { AuthUser, LoginRequest, SignupRequest } from "./auth.types";

export const authApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        currentUser: builder.query<AuthUser, void>({ query: () => "/auth/me" }),

        login: builder.mutation<AuthUser, LoginRequest>({
            query: (body) => ({ url: "/auth/login", method: "POST", body }),
            async onQueryStarted(_arg, { dispatch, queryFulfilled }) {
                try {
                    const { data } = await queryFulfilled;
                    dispatch(sessionReceived(data));
                } catch {
                    /* Form owns request errors. */
                }
            },
        }),

        signup: builder.mutation<AuthUser, SignupRequest>({
            query: (body) => ({ url: "/auth/signup", method: "POST", body }),
            async onQueryStarted(_arg, { dispatch, queryFulfilled }) {
                try {
                    const { data } = await queryFulfilled;
                    dispatch(sessionReceived(data));
                } catch {
                    /* Form owns request errors. */
                }
            },
        }),

        forgotPassword: builder.mutation<string, { email: string }>({
            query: (body) => ({
                url: "/auth/forgot-password",
                method: "POST",
                body,
                responseHandler: "text",
            }),
        }),

        requestPasswordChange: builder.mutation<string, void>({
            query: () => ({
                url: "/users/change-password",
                method: "POST",
                responseHandler: "text",
            }),
        }),

        resetPassword: builder.mutation<string, { token: string; password: string }>({
            query: (body) => ({
                url: "/users/reset-password/confirm",
                method: "POST",
                body,
                responseHandler: (response) =>
                    response.headers.get("content-type")?.includes("application/json")
                        ? response.json()
                        : response.text(),
            }),
        }),

        logout: builder.mutation<void, void>({
            query: () => ({ url: "/auth/logout", method: "POST" }),
        }),
    }),
});
export const {
    useCurrentUserQuery,
    useLoginMutation,
    useSignupMutation,
    useForgotPasswordMutation,
    useRequestPasswordChangeMutation,
    useResetPasswordMutation,
    useLogoutMutation,
} = authApi;
