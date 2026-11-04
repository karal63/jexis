import { z } from "zod";
import { passwordSchema } from "./password.schema";
import { en, type Translator } from "@/shared/i18n/en";

export const createResetPasswordSchema = (t: Translator) =>
    z
        .object({ password: passwordSchema(t), confirmPassword: z.string() })
        .refine((values) => values.password === values.confirmPassword, {
            path: ["confirmPassword"],
            message: t("validation.passwordMatch"),
        });
export const resetPasswordSchema = createResetPasswordSchema((key) => en[key]);

export type ResetPasswordValues = z.infer<typeof resetPasswordSchema>;
