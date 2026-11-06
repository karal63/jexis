import type { Account } from "./account.types";

export function accountName(account: Account): string {
    return (
        [account.firstName, account.lastName].filter(Boolean).join(" ").trim() ||
        account.email ||
        ""
    );
}
