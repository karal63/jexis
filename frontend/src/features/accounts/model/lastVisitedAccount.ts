const key = (userId: string) => `lastVisitedAccount:${userId}`;

export function getLastVisitedAccount(userId: string): string | null {
    try {
        return localStorage.getItem(key(userId));
    } catch {
        return null;
    }
}

export function setLastVisitedAccount(userId: string, accountId: string) {
    try {
        localStorage.setItem(key(userId), accountId);
    } catch {
        // Account navigation still works when browser storage is unavailable.
    }
}

export function clearLastVisitedAccount(userId: string, accountId: string) {
    try {
        if (localStorage.getItem(key(userId)) === accountId) localStorage.removeItem(key(userId));
    } catch {
        // Account navigation still works when browser storage is unavailable.
    }
}
