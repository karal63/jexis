import type { FieldValues, Path, UseFormSetError } from "react-hook-form";
import { getApiError } from "@/shared/api/apiError";

export function applyFormError<T extends FieldValues>(
    error: unknown,
    setError: UseFormSetError<T>,
    fields: readonly Path<T>[],
) {
    const normalized = getApiError(error);
    const unmatched: string[] = [];
    for (const item of normalized.fields) {
        const field = fields.find((field) => field === item.field);
        if (field)
            setError(field, { type: "server", message: item.message }, { shouldFocus: true });
        else unmatched.push(item.message);
    }
    if (!normalized.fields.length || unmatched.length) {
        setError("root.server", { message: unmatched.join(" ") || normalized.message });
    }
}
