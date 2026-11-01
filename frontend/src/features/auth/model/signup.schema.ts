import { z } from "zod";
import type { SignupRequest } from "../api/auth.types";
export const signupSchema = z
    .object({
        firstName: z.string().trim().min(1, "First name is required.").max(100),
        lastName: z.string().trim().min(1, "Last name is required.").max(100),
        email: z.email("Enter a valid email address."),
        phoneNumber: z
            .string()
            .trim()
            .regex(/^[\d+\-()\s]*$/, "Enter a valid phone number."),
        password: z
            .string()
            .min(8, "Password must be at least 8 characters.")
            .max(128)
            .refine((value) => value.trim().length > 0, "Password cannot be blank."),
        confirmPassword: z.string(),
    })
    .refine((values) => values.password === values.confirmPassword, {
        path: ["confirmPassword"],
        message: "Passwords do not match.",
    });
export type SignupValues = z.infer<typeof signupSchema>;
export function toSignupRequest({
    confirmPassword: _confirmation,
    ...values
}: SignupValues): SignupRequest {
    return { ...values, phoneNumber: values.phoneNumber || null };
}
