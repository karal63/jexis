import { useSignupMutation } from "../api/authApi";
import { toSignupRequest } from "../model/signup.schema";
import { PasswordInput } from "@/shared/ui/password-input";
import { createSignupSchema, type SignupValues } from "../model/signup.schema";
import { applyFormError } from "@/shared/forms/applyFormError";
import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { FormField as AuthField } from "@/shared/ui/form-field";
import { AuthForm, AuthSubmit, FormError } from "./AuthForm";
import { useI18n } from "@/shared/i18n/useI18n";
import { useEffect } from "react";

export function SignupForm({ onSuccess }: { onSuccess: () => void }) {
    const { t, locale } = useI18n();
    const {
        register,
        handleSubmit,
        setError,
        clearErrors,
        formState: { errors, isSubmitting },
    } = useForm<SignupValues>({
        resolver: zodResolver(createSignupSchema(t)),
        defaultValues: { phoneNumber: "" },
    });
    useEffect(() => clearErrors(), [clearErrors, locale]);

    const [signup] = useSignupMutation();
    async function submit(values: SignupValues) {
        try {
            await signup(toSignupRequest(values)).unwrap();
            onSuccess();
        } catch (error) {
            applyFormError(
                error,
                setError,
                ["firstName", "lastName", "email", "phoneNumber", "password"],
                t,
            );
        }
    }

    return (
        <>
            <AuthForm onSubmit={handleSubmit(submit)}>
                <div className="grid grid-cols-2 gap-3">
                    <AuthField
                        label={t("auth.firstName")}
                        autoComplete="given-name"
                        placeholder="Sam"
                        error={errors.firstName?.message}
                        {...register("firstName")}
                        name="firstName"
                    />
                    <AuthField
                        label={t("auth.lastName")}
                        autoComplete="family-name"
                        placeholder="Taylor"
                        error={errors.lastName?.message}
                        {...register("lastName")}
                        name="lastName"
                    />
                </div>
                <AuthField
                    label={t("auth.workEmail")}
                    type="email"
                    autoComplete="email"
                    placeholder="you@company.com"
                    error={errors.email?.message}
                    {...register("email")}
                    name="email"
                />
                <AuthField
                    label={t("auth.phone")}
                    error={errors.phoneNumber?.message}
                    type="tel"
                    maxLength={64}
                    hint={t("validation.phoneHint")}
                    autoComplete="tel"
                    required={false}
                    placeholder={t("auth.optional")}
                    {...register("phoneNumber")}
                    name="phoneNumber"
                />
                <PasswordInput
                    label={t("common.password")}
                    autoComplete="new-password"
                    required
                    error={errors.password?.message}
                    {...register("password")}
                />
                <PasswordInput
                    label={t("auth.confirmPassword")}
                    autoComplete="new-password"
                    required
                    error={errors.confirmPassword?.message}
                    {...register("confirmPassword")}
                />
                <FormError message={errors.root?.server?.message} />
                <AuthSubmit loading={isSubmitting}>
                    {t("auth.createButton")} <ArrowRight size={15} />
                </AuthSubmit>
                <p className="pt-4 text-center text-xs text-muted-foreground">
                    {t("auth.haveAccount")}{" "}
                    <Link className="font-semibold text-brand-leaf hover:underline" to="/login">
                        {t("common.signIn")}
                    </Link>
                </p>
            </AuthForm>
        </>
    );
}
