import { z } from "zod";
import { en, type Translator } from "@/shared/i18n/en";
export const createForgotPasswordSchema = (t: Translator) =>
    z.object({ email: z.email(t("validation.email")) });
export const forgotPasswordSchema = createForgotPasswordSchema((key) => en[key]);
export type ForgotPasswordValues = z.infer<typeof forgotPasswordSchema>;
