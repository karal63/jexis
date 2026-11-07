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
        }),
        createOnboardingLink: builder.mutation<AccountOnboarding, string>({
            query: (id) => ({ url: `/accounts/${id}/onboarding`, method: "POST" }),
        }),
        account: builder.query<Account, string>({
            query: (id) => `/accounts/${id}`,
        }),
    }),
});

export const {
    useMyAccountsQuery,
    useAccountQuery,
    useCreateAccountMutation,
    useAccountOnboardingQuery,
    useCreateOnboardingLinkMutation,
} = accountsApi;
