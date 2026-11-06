import { z } from "zod";
import { parsePhoneNumberFromString } from "libphonenumber-js/max";
import type { Translator } from "@/shared/i18n/en";
export function normalizePhone(value: string): string | null {
    if (value.length > 64) throw new Error("Invalid phone");
    const trimmed = value.trim();
    if (!trimmed) return null;
    if (!/^\+[0-9()\s-]+$/.test(trimmed)) throw new Error("Invalid phone");
    const phone = parsePhoneNumberFromString(trimmed, { extract: false });
    if (!phone?.isValid() || phone.ext) throw new Error("Invalid phone");
    return phone.number;
}
export const createProfileFields = (t: Translator) => ({
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
    phoneNumber: z.string().transform((value, ctx) => {
        try {
            return normalizePhone(value) ?? "";
        } catch {
            ctx.addIssue({ code: "custom", message: t("validation.phone") });
            return z.NEVER;
        }
    }),
});
