import { z } from "zod";
import { en, type Translator } from "@/shared/i18n/en";

export const passwordSchema = (t: Translator) =>
    z
        .string()
        .min(8, t("validation.passwordMin"))
        .max(128, t("validation.passwordMax"))
        .refine((value) => value.trim().length > 0, t("validation.passwordBlank"));
export const newPasswordSchema = passwordSchema((key) => en[key]);
