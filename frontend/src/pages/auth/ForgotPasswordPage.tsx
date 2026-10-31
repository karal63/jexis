import { useState, type FormEvent } from "react";
import { Link } from "react-router-dom";
import { ArrowLeft, ArrowRight, MailCheck } from "lucide-react";
import { AuthField, AuthForm, AuthLayout, AuthSubmit } from "@/features/auth/ui/AuthLayout";
import { api, getApiError } from "@/shared/config/api.ts";

export default function ForgotPasswordPage() {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [sent, setSent] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError("");
        setLoading(true);
        const values = new FormData(event.currentTarget);
        try {
            await api.post("/auth/forgot-password", { email: values.get("email") });
            setSent(true);
        } catch (requestError) {
            setError(getApiError(requestError));
        } finally {
            setLoading(false);
        }
    }

    return (
        <AuthLayout mode="forgot">
            {sent ? (
                <div className="rounded-lg border border-border bg-card p-6 text-center">
                    <span className="mx-auto grid size-12 place-items-center rounded-full bg-accent text-brand-leaf">
                        <MailCheck size={22} />
                    </span>
                    <h3 className="mt-4 font-display text-xl font-semibold text-foreground">
                        Check your inbox
                    </h3>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                        If there’s an account for that email, we’ve sent a link to reset your
                        password.
                    </p>
                    <Link
                        className="mt-5 inline-flex items-center gap-2 text-xs font-semibold text-brand-leaf"
                        to="/login"
                    >
                        <ArrowLeft size={14} /> Back to sign in
                    </Link>
                </div>
            ) : (
                <>
                    <div className="mb-6">
                        <p className="text-sm leading-6 text-muted-foreground">
                            Enter the email address you use for Jexis. We’ll send you a link to
                            reset your password.
                        </p>
                    </div>
                    <AuthForm onSubmit={handleSubmit}>
                        <AuthField
                            label="Email address"
                            name="email"
                            type="email"
                            autoComplete="email"
                            placeholder="you@company.com"
                        />
                        {error && (
                            <p
                                role="alert"
                                className="mb-3 rounded-md border border-danger-line bg-danger-bg px-3 py-2.5 text-xs leading-5 text-danger"
                            >
                                {error}
                            </p>
                        )}
                        <AuthSubmit loading={loading}>
                            Send reset link <ArrowRight size={15} />
                        </AuthSubmit>
                        <p className="pt-5 text-center text-xs">
                            <Link
                                className="font-semibold text-brand-leaf hover:underline"
                                to="/login"
                            >
                                Back to sign in
                            </Link>
                        </p>
                    </AuthForm>
                </>
            )}
        </AuthLayout>
    );
}
