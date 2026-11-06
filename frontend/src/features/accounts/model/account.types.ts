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
