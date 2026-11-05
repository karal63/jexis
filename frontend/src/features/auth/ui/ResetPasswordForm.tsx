import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowRight } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link } from "react-router-dom";
import { PasswordInput } from "@/shared/ui/password-input";
import { useResetPasswordMutation } from "../api/authApi";
import { applyFormError } from "../model/applyFormError";
import { createResetPasswordSchema, type ResetPasswordValues } from "../model/resetPassword.schema";
import { AuthForm, AuthSubmit, FormError } from "./AuthForm";
import { useI18n } from "@/shared/i18n/useI18n";
import { useEffect } from "react";

export function ResetPasswordForm({ token, onSuccess }: { token: string; onSuccess: () => void }) {
    const { t, locale } = useI18n();
    const {
        register,
        handleSubmit,
        setError,
        clearErrors,
        formState: { errors, isSubmitting },
    } = useForm<ResetPasswordValues>({ resolver: zodResolver(createResetPasswordSchema(t)) });
    useEffect(() => clearErrors(), [clearErrors, locale]);
    const [resetPassword] = useResetPasswordMutation();

    async function submit({ password }: ResetPasswordValues) {
        try {
            await resetPassword({ token, password }).unwrap();
            onSuccess();
        } catch (error) {
            applyFormError(error, setError, ["password"], t);
        }
    }

    return (
        <AuthForm onSubmit={handleSubmit(submit)}>
            <PasswordInput
                label={t("auth.newPassword")}
                autoComplete="new-password"
                required
                error={errors.password?.message}
                {...register("password")}
            />
            <PasswordInput
                label={t("auth.confirmNewPassword")}
                autoComplete="new-password"
                required
                error={errors.confirmPassword?.message}
                {...register("confirmPassword")}
            />
            <FormError message={errors.root?.server?.message} />
            <AuthSubmit loading={isSubmitting}>
                {t("auth.resetButton")} <ArrowRight size={15} />
            </AuthSubmit>
            <p className="pt-4 text-center text-xs text-muted-foreground">
                {t("auth.needLink")}{" "}
                <Link
                    className="font-semibold text-brand-leaf hover:underline"
                    to="/forgot-password"
                >
                    {t("common.requestOne")}
                </Link>
            </p>
        </AuthForm>
    );
}
