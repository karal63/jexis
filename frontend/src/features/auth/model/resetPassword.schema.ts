import { z } from "zod";
import { newPasswordSchema } from "./password.schema";

export const resetPasswordSchema = z
    .object({ password: newPasswordSchema, confirmPassword: z.string() })
    .refine((values) => values.password === values.confirmPassword, {
        path: ["confirmPassword"],
        message: "Passwords do not match.",
    });

export type ResetPasswordValues = z.infer<typeof resetPasswordSchema>;
