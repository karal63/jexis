import { z } from "zod";
import type { SignupRequest } from "../api/auth.types";
import { passwordSchema } from "./password.schema";
import { en, type Translator } from "@/shared/i18n/en";
export const createSignupSchema = (t: Translator) =>
    z
        .object({
            firstName: z
                .string()
                .trim()
                .min(1, t("validation.firstRequired"))
                .max(100, t("validation.firstMax")),
            lastName: z
                .string()
                .trim()
                .min(1, t("validation.lastRequired"))
                .max(100, t("validation.lastMax")),
            email: z.email(t("validation.email")),
            phoneNumber: z
                .string()
                .trim()
                .regex(/^[\d+\-()\s]*$/, t("validation.phone")),
            password: passwordSchema(t),
            confirmPassword: z.string(),
        })
        .refine((values) => values.password === values.confirmPassword, {
            path: ["confirmPassword"],
            message: t("validation.passwordMatch"),
        });
export const signupSchema = createSignupSchema((key) => en[key]);
export type SignupValues = z.infer<typeof signupSchema>;
export function toSignupRequest({
    confirmPassword: _confirmation,
    ...values
}: SignupValues): SignupRequest {
    return { ...values, phoneNumber: values.phoneNumber || null };
}
