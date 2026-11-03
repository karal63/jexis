import { z } from "zod";

export const newPasswordSchema = z
    .string()
    .min(8, "Password must be at least 8 characters.")
    .max(128, "Password must be at most 128 characters.")
    .refine((value) => value.trim().length > 0, "Password cannot be blank.");
