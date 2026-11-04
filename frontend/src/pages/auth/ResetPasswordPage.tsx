import { useState } from "react";
import { CheckCircle2 } from "lucide-react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useAppDispatch } from "@/app/hooks";
import { AuthLayout } from "@/features/auth";
import { ResetPasswordForm } from "@/features/auth/ui/ResetPasswordForm";
import { sessionEnded } from "@/shared/api/sessionEvents";
import { useI18n } from "@/shared/i18n/useI18n";

export default function ResetPasswordPage() {
    const { t } = useI18n();
    const [searchParams] = useSearchParams();
    const [complete, setComplete] = useState(false);
    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const token = searchParams.get("token")?.trim();

    function onSuccess() {
        setComplete(true);
        dispatch(sessionEnded());
        navigate("/reset-password", { replace: true });
    }

    return (
        <AuthLayout mode="reset">
            {complete ? (
                <div className="rounded-lg border border-border bg-card p-6 text-center">
                    <span className="mx-auto grid size-12 place-items-center rounded-full bg-accent text-brand-leaf">
                        <CheckCircle2 size={22} />
                    </span>
                    <h2 className="mt-4 font-display text-xl font-semibold">{t("auth.updated")}</h2>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                        {t("auth.updatedText")}
                    </p>
                    <Link
                        className="mt-5 inline-block text-sm font-semibold text-brand-leaf hover:underline"
                        to="/login"
                    >
                        {t("common.signIn")}
                    </Link>
                </div>
            ) : token ? (
                <ResetPasswordForm token={token} onSuccess={onSuccess} />
            ) : (
                <div className="rounded-lg border border-border bg-card p-6 text-center">
                    <h2 className="font-display text-xl font-semibold">{t("auth.linkMissing")}</h2>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                        {t("auth.linkMissingText")}
                    </p>
                    <Link
                        className="mt-5 inline-block text-sm font-semibold text-brand-leaf hover:underline"
                        to="/forgot-password"
                    >
                        {t("auth.requestLink")}
                    </Link>
                </div>
            )}
        </AuthLayout>
    );
}
