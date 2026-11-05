import { useForgotPasswordMutation } from "../api/authApi";
import {
    createForgotPasswordSchema,
    type ForgotPasswordValues,
} from "../model/forgotPassword.schema";
import { applyFormError } from "../model/applyFormError";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowLeft, ArrowRight, MailCheck } from "lucide-react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { FormField as AuthField } from "@/shared/ui/form-field";
import { AuthForm, AuthSubmit, FormError } from "./AuthForm";
import { useI18n } from "@/shared/i18n/useI18n";

export function ForgotPasswordForm() {
    const { t, locale } = useI18n();
    const [sent, setSent] = useState(false);
    const {
        register,
        handleSubmit,
        setError,
        clearErrors,
        formState: { errors, isSubmitting },
    } = useForm<ForgotPasswordValues>({
        resolver: zodResolver(createForgotPasswordSchema(t)),
    });
    useEffect(() => clearErrors(), [clearErrors, locale]);

    const [forgotPassword] = useForgotPasswordMutation();
    async function submit(values: ForgotPasswordValues) {
        try {
            await forgotPassword(values).unwrap();
            setSent(true);
        } catch (error) {
            applyFormError(error, setError, ["email"], t);
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
                        {t("auth.checkInbox")}
                    </h3>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">{t("auth.sent")}</p>
                    <Link
                        className="mt-5 inline-flex items-center gap-2 text-xs font-semibold text-brand-leaf"
                        to="/login"
                    >
                        <ArrowLeft size={14} /> {t("common.backToSignIn")}
                    </Link>
                </div>
            ) : (
                <>
                    <div className="mb-6">
                        <p className="text-sm leading-6 text-muted-foreground">
                            {t("auth.forgotHelp")}
                        </p>
                    </div>
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
                        <FormError message={errors.root?.server?.message} />
                        <AuthSubmit loading={isSubmitting}>
                            {t("auth.sendReset")} <ArrowRight size={15} />
                        </AuthSubmit>
                        <p className="pt-5 text-center text-xs">
                            <Link
                                className="font-semibold text-brand-leaf hover:underline"
                                to="/login"
                            >
                                {t("common.backToSignIn")}
                            </Link>
                        </p>
                    </AuthForm>
                </>
            )}
        </>
    );
}
