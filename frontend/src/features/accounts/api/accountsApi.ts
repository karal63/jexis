import { baseApi } from "@/shared/api/baseApi";

export interface Account {
    id: string;
    firstName: string | null;
    lastName: string | null;
    email: string | null;
    city: string | null;
    country: string | null;
    owner: { id: string };
}

export interface AccountPage {
    items: Account[];
    page: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
}

export function accountName(account: Account): string {
    return (
        [account.firstName, account.lastName].filter(Boolean).join(" ").trim() ||
        account.email ||
        ""
    );
}

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
