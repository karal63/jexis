import { useForgotPasswordMutation } from "../api/authApi";
import { forgotPasswordSchema, type ForgotPasswordValues } from "../model/forgotPassword.schema";
import { applyFormError } from "../model/applyFormError";
import { useState } from "react";
import { Link } from "react-router-dom";
import { ArrowLeft, ArrowRight, MailCheck } from "lucide-react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { FormField as AuthField } from "@/shared/ui/form-field";
import { AuthForm, AuthSubmit, FormError } from "./AuthForm";

export function ForgotPasswordForm() {
    const [sent, setSent] = useState(false);
    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<ForgotPasswordValues>({
        resolver: zodResolver(forgotPasswordSchema),
    });

    const [forgotPassword] = useForgotPasswordMutation();
    async function submit(values: ForgotPasswordValues) {
        try {
            await forgotPassword(values).unwrap();
            setSent(true);
        } catch (error) {
            applyFormError(error, setError, ["email"]);
        }
    }

    return (
        <>
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
                    <AuthForm onSubmit={handleSubmit(submit)}>
                        <AuthField
                            label="Email address"
                            type="email"
                            autoComplete="email"
                            placeholder="you@company.com"
                            error={errors.email?.message}
                            {...register("email")}
                            name="email"
                        />
                        <FormError message={errors.root?.server?.message} />
                        <AuthSubmit loading={isSubmitting}>
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
        </>
    );
}
