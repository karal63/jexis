import { baseApi } from "@/shared/api/baseApi";

import type { Account, AccountPage } from "../model/account.types";

export const accountsApi = baseApi.injectEndpoints({
    endpoints: (builder) => ({
        myAccounts: builder.query<AccountPage, { page: number; search: string }>({
            query: ({ page, search }) => ({
                url: "/accounts/me",
                params: { page, pageSize: 12, ...(search ? { search } : {}) },
            }),
        }),
        account: builder.query<Account, string>({
            query: (id) => `/accounts/${id}`,
        }),
    }),
});

export const { useMyAccountsQuery, useAccountQuery } = accountsApi;
