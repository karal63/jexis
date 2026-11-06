import { useLoginMutation } from "../api/authApi";
import { PasswordInput } from "@/shared/ui/password-input";
import { createLoginSchema, type LoginValues } from "../model/login.schema";
import { applyFormError } from "@/shared/forms/applyFormError";
import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { FormField as AuthField } from "@/shared/ui/form-field";
import { AuthForm, AuthSubmit, FormError } from "./AuthForm";
import { useI18n } from "@/shared/i18n/useI18n";
import { useEffect } from "react";

export function LoginForm({ onSuccess }: { onSuccess: () => void }) {
    const { t, locale } = useI18n();
    const {
        register,
        handleSubmit,
        setError,
        clearErrors,
        formState: { errors, isSubmitting },
    } = useForm<LoginValues>({
        resolver: zodResolver(createLoginSchema(t)),
    });
    useEffect(() => clearErrors(), [clearErrors, locale]);

    const [login] = useLoginMutation();
    async function submit(values: LoginValues) {
        try {
            await login(values).unwrap();
            onSuccess();
        } catch (error) {
            applyFormError(error, setError, ["email", "password"], t);
        }
    }

    return (
        <>
            <AuthForm onSubmit={handleSubmit(submit)}>
                <AuthField
                    label={t("common.email")}
                    type="email"
                    autoComplete="email"
                    placeholder="you@company.com"
                    error={errors.email?.message}
                    {...register("email")}
                    name="email"
                />
                <PasswordInput
                    label={t("common.password")}
                    autoComplete="current-password"
                    required
                    error={errors.password?.message}
                    {...register("password")}
                />
                <div className="-mt-1 mb-2 flex justify-end">
                    <Link
                        className="text-[11px] font-semibold text-brand-leaf hover:underline"
                        to="/forgot-password"
                    >
                        {t("auth.forgot")}
                    </Link>
                </div>
                <FormError message={errors.root?.server?.message} />
                <AuthSubmit loading={isSubmitting}>
                    {t("common.signIn")} <ArrowRight size={15} />
                </AuthSubmit>
                <p className="pt-5 text-center text-xs text-muted-foreground">
                    {t("auth.newHere")}{" "}
                    <Link className="font-semibold text-brand-leaf hover:underline" to="/signup">
                        {t("auth.createLink")}
                    </Link>
                </p>
            </AuthForm>
        </>
    );
}
