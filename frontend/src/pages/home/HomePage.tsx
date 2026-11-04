import { useState } from "react";
import { useLogoutMutation } from "@/features/auth";
import { useAppDispatch, useAppSelector } from "@/app/hooks";
import { sessionEnded } from "@/shared/api/sessionEvents";
import { getApiError } from "@/shared/api/apiError";
import { Button } from "@/shared/ui/button";
import { setTheme, type Theme } from "@/features/theme/store/themeSlice";
import { LanguageSelect } from "@/shared/ui/language-select";
import { useI18n } from "@/shared/i18n/useI18n";

export default function HomePage() {
    const { t } = useI18n();
    const user = useAppSelector((state) => state.session.user);
    const theme = useAppSelector((state) => state.theme.theme);
    const dispatch = useAppDispatch();
    const [logout, { isLoading }] = useLogoutMutation();
    const [error, setError] = useState<unknown>(null);

    async function signOut() {
        setError(null);
        try {
            await logout().unwrap();
            dispatch(sessionEnded());
        } catch (error) {
            setError(error);
        }
    }

    return (
        <main className="min-h-screen bg-background px-6 py-12 text-foreground">
            <section className="mx-auto max-w-3xl space-y-6">
                <div className="flex justify-end">
                    <LanguageSelect />
                </div>
                <h1 className="font-display text-3xl font-bold">
                    {t("home.welcome", { name: user?.firstName ?? "" })}
                </h1>
                <p className="text-muted-foreground">{t("home.ready")}</p>
                <label className="flex items-center gap-3">
                    {t("home.appearance")}
                    <select
                        className="rounded-md border bg-card p-2"
                        value={theme}
                        onChange={(event) => dispatch(setTheme(event.target.value as Theme))}
                    >
                        <option value="system">{t("home.system")}</option>
                        <option value="light">{t("home.light")}</option>
                        <option value="dark">{t("home.dark")}</option>
                    </select>
                </label>
                {error != null && (
                    <p role="alert" className="text-danger">
                        {getApiError(error, t).message}
                    </p>
                )}
                <Button onClick={signOut} disabled={isLoading}>
                    {isLoading ? t("home.signingOut") : t("home.signOut")}
                </Button>
            </section>
        </main>
    );
}
