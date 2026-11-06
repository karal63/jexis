import { useEffect, useRef, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { useAppSelector } from "@/app/hooks";
import { useUpdatePersonalInfoMutation } from "@/features/profile/api/profileApi";
import { applyFormError } from "@/shared/forms/applyFormError";
import { getApiError } from "@/shared/api/apiError";
import { useI18n } from "@/shared/i18n/useI18n";
import { Button } from "@/shared/ui/button";
import { FormField } from "@/shared/ui/form-field";
import { createPersonalInfoSchema, type PersonalInfoValues } from "../model/personalInfo.schema";
export function PersonalInfoForm() {
    const { t, locale } = useI18n();
    const user = useAppSelector((state) => state.session.user);
    const [updatePersonalInfo] = useUpdatePersonalInfoMutation();
    const [saved, setSaved] = useState(false);
    const {
        register,
        handleSubmit,
        reset,
        setError,
        clearErrors,
        formState: { errors, isSubmitting, isDirty, dirtyFields },
    } = useForm<PersonalInfoValues>({
        resolver: zodResolver(createPersonalInfoSchema(t)),
        defaultValues: {
            firstName: user?.firstName ?? "",
            lastName: user?.lastName ?? "",
            phoneNumber: user?.phoneNumber ?? "",
        },
    });

    useEffect(() => {
        if (user)
            reset(
                {
                    firstName: user.firstName,
                    lastName: user.lastName,
                    phoneNumber: user.phoneNumber ?? "",
                },
                { keepDirtyValues: true },
            );
    }, [user, reset]);
    useEffect(() => clearErrors(), [clearErrors, locale]);
    void dirtyFields; // Subscribe so reset keeps dirty field values.
    const mounted = useRef(true);
    useEffect(() => {
        mounted.current = true;
        return () => {
            mounted.current = false;
        };
    }, []);

    async function savePersonalInfo(values: PersonalInfoValues) {
        if (!user) return;
        setSaved(false);
        try {
            const updated = await updatePersonalInfo({
                id: user.id,
                firstName: values.firstName,
                lastName: values.lastName,
                phoneNumber: values.phoneNumber || null,
            }).unwrap();
            if (!mounted.current) return;
            reset({
                firstName: updated.firstName,
                lastName: updated.lastName,
                phoneNumber: updated.phoneNumber ?? "",
            });
            setSaved(true);
        } catch (error) {
            if (!mounted.current) return;
            const apiError = getApiError(error, t);
            if (apiError.code === "PHONE_NUMBER_EXISTS") {
                setError(
                    "phoneNumber",
                    { type: "server", message: apiError.message },
                    { shouldFocus: true },
                );
            } else {
                applyFormError(error, setError, ["firstName", "lastName", "phoneNumber"], t);
            }
        }
    }

    return (
        <section aria-labelledby="personal-info-heading">
            <h2 id="personal-info-heading" className="font-display text-lg font-bold">
                {t("settings.personalInfo")}
            </h2>
            <p className="mt-1 text-sm text-muted-foreground">
                {t("settings.personalInfoDescription")}
            </p>
            <form
                onSubmit={(event) => void handleSubmit(savePersonalInfo)(event)}
                noValidate
                className="mt-4 rounded-lg border border-border p-5"
            >
                <fieldset disabled={isSubmitting}>
                    <div className="grid gap-x-4 sm:grid-cols-2">
                        <FormField
                            label={t("auth.firstName")}
                            autoComplete="given-name"
                            required
                            error={errors.firstName?.message}
                            {...register("firstName")}
                        />
                        <FormField
                            label={t("auth.lastName")}
                            autoComplete="family-name"
                            required
                            error={errors.lastName?.message}
                            {...register("lastName")}
                        />
                    </div>
                    <FormField
                        label={t("auth.phone")}
                        type="tel"
                        maxLength={64}
                        hint={t("validation.phoneHint")}
                        autoComplete="tel"
                        required={false}
                        placeholder={t("auth.optional")}
                        error={errors.phoneNumber?.message}
                        {...register("phoneNumber")}
                    />
                    {errors.root?.server?.message && (
                        <p role="alert" className="text-sm text-danger">
                            {errors.root.server.message}
                        </p>
                    )}
                    <div className="mt-3 flex flex-wrap items-center gap-3">
                        <Button type="submit" disabled={!isDirty || isSubmitting}>
                            {isSubmitting ? t("common.wait") : t("settings.saveChanges")}
                        </Button>
                        {saved && !isDirty && (
                            <p role="status" className="text-sm text-brand-leaf">
                                {t("settings.saved")}
                            </p>
                        )}
                    </div>
                </fieldset>
            </form>
        </section>
    );
}
