import { useState } from "react";
import { CheckCircle2 } from "lucide-react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useAppDispatch } from "@/app/hooks";
import { AuthLayout } from "@/features/auth";
import { ResetPasswordForm } from "@/features/auth/ui/ResetPasswordForm";
import { sessionEnded } from "@/shared/api/sessionEvents";

export default function ResetPasswordPage() {
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
                    <h2 className="mt-4 font-display text-xl font-semibold">Password updated</h2>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                        Your password has been changed. Sign in with your new password.
                    </p>
                    <Link
                        className="mt-5 inline-block text-sm font-semibold text-brand-leaf hover:underline"
                        to="/login"
                    >
                        Sign in
                    </Link>
                </div>
            ) : token ? (
                <ResetPasswordForm token={token} onSuccess={onSuccess} />
            ) : (
                <div className="rounded-lg border border-border bg-card p-6 text-center">
                    <h2 className="font-display text-xl font-semibold">Reset link missing</h2>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                        Open the link from your email or request a new one.
                    </p>
                    <Link
                        className="mt-5 inline-block text-sm font-semibold text-brand-leaf hover:underline"
                        to="/forgot-password"
                    >
                        Request a reset link
                    </Link>
                </div>
            )}
        </AuthLayout>
    );
}
