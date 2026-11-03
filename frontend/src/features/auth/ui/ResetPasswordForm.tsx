import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowRight } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link } from "react-router-dom";
import { PasswordInput } from "@/shared/ui/password-input";
import { getApiError } from "@/shared/api/apiError";
import { useResetPasswordMutation } from "../api/authApi";
import { applyFormError } from "../model/applyFormError";
import { resetPasswordSchema, type ResetPasswordValues } from "../model/resetPassword.schema";
import { AuthForm, AuthSubmit, FormError } from "./AuthForm";

export function ResetPasswordForm({ token, onSuccess }: { token: string; onSuccess: () => void }) {
    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<ResetPasswordValues>({ resolver: zodResolver(resetPasswordSchema) });
    const [resetPassword] = useResetPasswordMutation();

    async function submit({ password }: ResetPasswordValues) {
        try {
            await resetPassword({ token, password }).unwrap();
            onSuccess();
        } catch (error) {
            if (getApiError(error).code === "INVALID_PASSWORD_RESET_TOKEN") {
                setError("root.server", {
                    message: "This reset link is invalid or has expired. Request a new one.",
                });
            } else {
                applyFormError(error, setError, ["password"]);
            }
        }
    }

    return (
        <AuthForm onSubmit={handleSubmit(submit)}>
            <PasswordInput
                label="New password"
                autoComplete="new-password"
                required
                error={errors.password?.message}
                {...register("password")}
            />
            <PasswordInput
                label="Confirm new password"
                autoComplete="new-password"
                required
                error={errors.confirmPassword?.message}
                {...register("confirmPassword")}
            />
            <FormError message={errors.root?.server?.message} />
            <AuthSubmit loading={isSubmitting}>
                Reset password <ArrowRight size={15} />
            </AuthSubmit>
            <p className="pt-4 text-center text-xs text-muted-foreground">
                Need a new link?{" "}
                <Link
                    className="font-semibold text-brand-leaf hover:underline"
                    to="/forgot-password"
                >
                    Request one
                </Link>
            </p>
        </AuthForm>
    );
}
