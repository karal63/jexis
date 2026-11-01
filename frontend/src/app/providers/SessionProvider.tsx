import { useEffect, type ReactNode } from "react";
import { useCurrentUserQuery, sessionReceived } from "@/features/auth";
import { useAppDispatch, useAppSelector } from "../hooks";
import { Button } from "@/shared/ui/button";

export function SessionProvider({ children }: { children: ReactNode }) {
    const dispatch = useAppDispatch();
    const status = useAppSelector((state) => state.session.status);
    const { data, error, refetch } = useCurrentUserQuery(undefined, {
        skip: status !== "checking",
    });

    useEffect(() => {
        if (data && status === "checking") dispatch(sessionReceived(data));
    }, [data, status, dispatch]);

    if (status === "checking")
        return (
            <main className="grid min-h-screen place-content-center gap-4 bg-background p-6 text-foreground">
                {error ? (
                    <>
                        <p role="alert">We couldn’t check your session. Please try again.</p>
                        <Button onClick={() => refetch()}>Try again</Button>
                    </>
                ) : (
                    <p role="status">Checking your session…</p>
                )}
            </main>
        );
    return children;
}
