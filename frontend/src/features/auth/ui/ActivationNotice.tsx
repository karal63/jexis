import { useAppSelector } from "@/app/hooks";
import { getApiError } from "@/shared/api/apiError";
import { useI18n } from "@/shared/i18n/useI18n";
import { cn } from "@/shared/lib/utils";
import { Button } from "@/shared/ui/button";
import { useResendActivationEmailMutation } from "../api/authApi";

export function ActivationNotice({ className }: { className: string }) {
    const { t } = useI18n();
    const { user, generation } = useAppSelector((state) => state.session);
    const [resend, { isLoading, isSuccess, error }] = useResendActivationEmailMutation({
        fixedCacheKey: `activation-email-${generation}`,
    });

    if (!user || user.isActivated) return null;

    return (
        <div className={cn("flex flex-wrap items-center justify-between gap-2", className)}>
            <div className="text-sm">
                <p role="status">{t(isSuccess ? "activation.emailSent" : "activation.message")}</p>
                {error && (
                    <p role="alert" className="text-danger">
                        {getApiError(error, t).message}
                    </p>
                )}
            </div>
            <Button size="small" disabled={isLoading} onClick={() => void resend()}>
                {t(isLoading ? "common.wait" : "activation.sendEmail")}
            </Button>
        </div>
    );
}
