import type { FieldValues, Path, UseFormSetError } from "react-hook-form";
import { getApiError, getFieldErrorMessage } from "@/shared/api/apiError";
import { en, type Translator } from "@/shared/i18n/en";

const english: Translator = (key) => en[key];

export function applyFormError<T extends FieldValues>(
    error: unknown,
    setError: UseFormSetError<T>,
    fields: readonly Path<T>[],
    t: Translator = english,
) {
    const normalized = getApiError(error, t);
    const unmatched: string[] = [];
    for (const item of normalized.fields) {
        const field = fields.find((field) => field === item.field);
        const message = item.code
            ? getFieldErrorMessage(item.field, item.code, t)
            : t("validation.invalid");
        if (field) setError(field, { type: "server", message }, { shouldFocus: true });
        else unmatched.push(message);
    }
    if (!normalized.fields.length || unmatched.length) {
        setError("root.server", { message: unmatched.join(" ") || normalized.message });
    }
}
