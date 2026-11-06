import { z } from "zod";
import { en, type TranslationKey, type Translator } from "@/shared/i18n/en";

const english: Translator = (key) => en[key];

const payloadSchema = z.object({
    code: z.string().optional(),
    message: z.string().optional(),
    errors: z
        .array(
            z.object({
                field: z.string(),
                code: z.string().optional(),
                message: z.string().optional(),
            }),
        )
        .optional(),
});

export interface ApiError {
    status?: number | string;
    code?: string;
    message: string;
    fields: { field: string; code?: string; message?: string }[];
}
function translatedCode(code: string | undefined, t: Translator): string | undefined {
    const key = `api.${code}` as TranslationKey;
    return code && Object.hasOwn(en, key) ? t(key) : undefined;
}
export function getApiError(error: unknown, t: Translator = english): ApiError {
    const parsed = z
        .object({ status: z.union([z.number(), z.string()]), data: z.unknown().optional() })
        .safeParse(error);
    const payload = payloadSchema.safeParse(parsed.success ? parsed.data.data : undefined);
    const status = parsed.success ? parsed.data.status : undefined;
    return {
        status,
        code: payload.success ? payload.data.code : undefined,
        fields: payload.success ? (payload.data.errors ?? []) : [],
        message:
            translatedCode(payload.success ? payload.data.code : undefined, t) ??
            (status === 429
                ? t("api.rateLimit")
                : status === 401
                  ? t("api.session")
                  : typeof status === "number"
                    ? t("api.generic")
                    : t("api.network")),
    };
}
export function getFieldErrorMessage(
    field: string,
    code: string | undefined,
    t: Translator,
): string {
    if (
        code === "VALIDATION_NOT_BLANK" ||
        code === "VALIDATION_NOT_NULL" ||
        code === "VALIDATION_NOT_EMPTY"
    )
        return t(
            field === "firstName"
                ? "validation.firstRequired"
                : field === "lastName"
                  ? "validation.lastRequired"
                  : "validation.required",
        );
    if (code === "VALIDATION_EMAIL") return t("validation.email");
    if ((code === "VALIDATION_PATTERN" || code === "VALIDATION_PHONE") && field === "phoneNumber")
        return t("validation.phone");
    if (code === "VALIDATION_SIZE")
        return t(
            field === "password"
                ? "validation.passwordSize"
                : field === "firstName"
                  ? "validation.firstMax"
                  : field === "lastName"
                    ? "validation.lastMax"
                    : "validation.size",
        );
    return t("validation.invalid");
}
