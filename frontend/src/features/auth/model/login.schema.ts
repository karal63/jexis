import { z } from "zod";
import { passwordSchema } from "./password.schema";
import { en, type Translator } from "@/shared/i18n/en";

export const createLoginSchema = (t: Translator) =>
    z.object({
        email: z.email(t("validation.email")),
        password: passwordSchema(t),
    });
export const loginSchema = createLoginSchema((key) => en[key]);

export type LoginValues = z.infer<typeof loginSchema>;
