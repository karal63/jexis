import { useEffect, type ReactNode } from "react";
import { useCurrentUserQuery, sessionReceived } from "@/features/auth";
import { useAppDispatch, useAppSelector } from "../hooks";
import { Button } from "@/shared/ui/button";
import { PageLoader } from "@/shared/ui/page-loader";
import { useI18n } from "@/shared/i18n/useI18n";

export function SessionProvider({ children }: { children: ReactNode }) {
    const { t } = useI18n();
    const dispatch = useAppDispatch();
    const status = useAppSelector((state) => state.session.status);
    const { data, error, refetch } = useCurrentUserQuery(undefined, {
        skip: status !== "checking",
    });

    useEffect(() => {
        if (data && status === "checking") dispatch(sessionReceived(data));
    }, [data, status, dispatch]);

    if (status === "checking" && !error) return <PageLoader label={t("session.checking")} />;

    if (status === "checking")
        return (
            <main className="grid min-h-screen place-content-center gap-4 bg-background p-6 text-foreground">
                <p role="alert">{t("session.checkFailed")}</p>
                <Button onClick={() => refetch()}>{t("session.retry")}</Button>
            </main>
        );
    return children;
}
