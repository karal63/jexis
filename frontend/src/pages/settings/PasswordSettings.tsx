import { useCallback, useState } from "react";
import { KeyRound } from "lucide-react";
import { useAppSelector } from "@/app/hooks";
import { useRequestPasswordChangeMutation } from "@/features/auth/api/authApi";
import { getApiError } from "@/shared/api/apiError";
import { useI18n } from "@/shared/i18n/useI18n";
import { AlertDialog } from "@/shared/ui/alert-dialog";
import { Button } from "@/shared/ui/button";

export function PasswordSettings() {
    const { t } = useI18n();
    const user = useAppSelector((state) => state.session.user);
    const [requestPasswordChange, { isLoading: isRequestingPasswordChange }] =
        useRequestPasswordChangeMutation();
    const [resetLinkSent, setResetLinkSent] = useState(false);
    const [passwordError, setPasswordError] = useState<string | null>(null);
    const closeAlert = useCallback(() => setResetLinkSent(false), []);

    async function sendPasswordResetLink() {
        if (!user || isRequestingPasswordChange) return;
        setPasswordError(null);
        try {
            await requestPasswordChange().unwrap();
            setResetLinkSent(true);
        } catch (error) {
            setPasswordError(getApiError(error, t).message);
        }
    }

    return (
        <>
            <section aria-labelledby="password-heading">
                <h2 id="password-heading" className="font-display text-lg font-bold">
                    {t("settings.changePassword")}
                </h2>
                <p className="mt-1 text-sm text-muted-foreground">
                    {t("settings.changePasswordDescription")}
                </p>
                <div className="mt-4 flex flex-col gap-4 rounded-lg border border-border px-5 py-5 sm:flex-row sm:items-center">
                    <KeyRound aria-hidden="true" size={18} className="shrink-0 text-brand-leaf" />
                    <p className="min-w-0 flex-1 break-all text-sm text-foreground">
                        {user?.email}
                    </p>
                    <Button
                        onClick={sendPasswordResetLink}
                        disabled={isRequestingPasswordChange}
                        className="shrink-0"
                    >
                        {isRequestingPasswordChange
                            ? t("common.wait")
                            : t("settings.sendResetLink")}
                    </Button>
                </div>
                {passwordError && (
                    <p role="alert" className="mt-2 text-sm text-danger">
                        {passwordError}
                    </p>
                )}
            </section>
            {resetLinkSent && user && (
                <AlertDialog
                    title={t("settings.resetLinkTitle")}
                    message={t("settings.resetLinkSent", { email: user.email })}
                    closeLabel={t("settings.closeAlert")}
                    onClose={closeAlert}
                />
            )}
        </>
    );
}
