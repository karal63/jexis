import { baseApi } from "@/shared/api/baseApi";

import type { Account, AccountOnboarding, AccountPage } from "../model/account.types";

export const accountsApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        myAccounts: builder.query<AccountPage, { page: number; search: string }>({
            query: ({ page, search }) => ({
                url: "/accounts/me",
                params: { page, pageSize: 12, ...(search ? { search } : {}) },
            }),
            providesTags: ["Accounts"],
        }),
        createAccount: builder.mutation<Account, void>({
            query: () => ({ url: "/accounts/create", method: "POST" }),
            invalidatesTags: (_result, error) => (error ? [] : ["Accounts"]),
        }),
        accountOnboarding: builder.query<AccountOnboarding, string>({
            query: (id) => ({ url: `/accounts/${id}/onboarding`, cache: "no-store" }),
            providesTags: (_result, _error, id) => [{ type: "Account", id }],
        }),
        createOnboardingLink: builder.mutation<AccountOnboarding, string>({
            query: (id) => ({ url: `/accounts/${id}/onboarding`, method: "POST" }),
        }),
        bankAccountSession: builder.mutation<
            { clientSecret: string },
            { id: string; manage: boolean }
        >({
            query: ({ id, manage }) => ({
                url: `/accounts/${id}/bank-account-session`,
                method: "POST",
                params: { manage },
            }),
        }),
        payoutBankSetup: builder.mutation<{ clientSecret: string; name: string }, string>({
            query: (id) => ({ url: `/accounts/${id}/payout-bank-setup`, method: "POST" }),
        }),
        completePayoutBankSetup: builder.mutation<void, { id: string; setupIntentId: string }>({
            query: ({ id, setupIntentId }) => ({
                url: `/accounts/${id}/payout-bank-setup/${setupIntentId}/complete`,
                method: "POST",
            }),
        }),
        account: builder.query<Account, string>({
            query: (id) => `/accounts/${id}`,
            providesTags: (_result, _error, id) => [{ type: "Account", id }],
        }),
        deleteAccount: builder.mutation<string, string>({
            query: (id) => ({
                url: `/accounts/${id}/delete`,
                method: "POST",
                responseHandler: "content-type",
            }),
            invalidatesTags: (_result, error, id) =>
                error ? [] : ["Accounts", { type: "Account", id }],
        }),
    }),
});

export const {
    useMyAccountsQuery,
    useAccountQuery,
    useCreateAccountMutation,
    useAccountOnboardingQuery,
    useCreateOnboardingLinkMutation,
    useBankAccountSessionMutation,
    usePayoutBankSetupMutation,
    useCompletePayoutBankSetupMutation,
    useDeleteAccountMutation,
} = accountsApi;
