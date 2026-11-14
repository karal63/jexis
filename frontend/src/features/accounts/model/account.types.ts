export interface Account {
    id: string;
    firstName: string | null;
    lastName: string | null;
    city: string | null;
    country: string | null;
    line1: string | null;
    line2: string | null;
    postalCode: string | null;
    state: string | null;
    phone: string | null;
    email: string | null;
    connectAccountId: string;
    owner: { id: string };
    createdAt: Date;
    updatedAt: Date;
}

export interface AccountOnboarding {
    status: "NOT_STARTED" | "ACTION_REQUIRED" | "PENDING_VERIFICATION" | "COMPLETED" | "RESTRICTED";
    canOnboard: boolean;
    canDelete: boolean;
    externalAccountRequired: boolean;
    url: string | null;
}

export interface AccountPage {
    items: Account[];
    page: number;
    pageSize: number;
    totalItems: number;
    totalPages: number;
}
