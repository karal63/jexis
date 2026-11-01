import { useState } from "react";
import { useLogoutMutation } from "@/features/auth";
import { useAppDispatch, useAppSelector } from "@/app/hooks";
import { sessionEnded } from "@/shared/api/sessionEvents";
import { getApiError } from "@/shared/api/apiError";
import { Button } from "@/shared/ui/button";
import { setTheme, type Theme } from "@/features/theme/store/themeSlice";

export default function HomePage() {
    const user = useAppSelector((state) => state.session.user);
    const theme = useAppSelector((state) => state.theme.theme);
    const dispatch = useAppDispatch();
    const [logout, { isLoading }] = useLogoutMutation();
    const [error, setError] = useState("");

    async function signOut() {
        setError("");
        try {
            await logout().unwrap();
            dispatch(sessionEnded());
        } catch (error) {
            setError(getApiError(error).message);
        }
    }

    return (
        <main className="min-h-screen bg-background px-6 py-12 text-foreground">
            <section className="mx-auto max-w-3xl space-y-6">
                <h1 className="font-display text-3xl font-bold">Welcome, {user?.firstName}</h1>
                <p className="text-muted-foreground">Your Jexis workspace is ready.</p>
                <label className="flex items-center gap-3">
                    Appearance
                    <select
                        className="rounded-md border bg-card p-2"
                        value={theme}
                        onChange={(event) => dispatch(setTheme(event.target.value as Theme))}
                    >
                        <option value="system">System</option>
                        <option value="light">Light</option>
                        <option value="dark">Dark</option>
                    </select>
                </label>
                {error && (
                    <p role="alert" className="text-danger">
                        {error}
                    </p>
                )}
                <Button onClick={signOut} disabled={isLoading}>
                    {isLoading ? "Signing out…" : "Sign out"}
                </Button>
            </section>
        </main>
    );
}
