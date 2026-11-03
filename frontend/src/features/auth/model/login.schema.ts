import { z } from "zod";
import { newPasswordSchema } from "./password.schema";

export const loginSchema = z.object({
    email: z.email("Enter a valid email address."),
    password: newPasswordSchema,
});

export type LoginValues = z.infer<typeof loginSchema>;
