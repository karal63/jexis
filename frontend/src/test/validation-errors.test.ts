import { describe, expect, it } from "vitest";
import { getFieldErrorMessage } from "@/shared/api/apiError";
import { en } from "@/shared/i18n/en";
import { pl } from "@/shared/i18n/pl";

describe("validation error contract", () => {
    it.each(["VALIDATION_NOT_BLANK", "VALIDATION_NOT_NULL", "VALIDATION_NOT_EMPTY"])(
        "translates %s for required fields",
        (code) => {
            for (const messages of [en, pl]) {
                const t = (key: keyof typeof en) => messages[key];
                expect(getFieldErrorMessage("firstName", code, t)).toBe(
                    messages["validation.firstRequired"],
                );
                expect(getFieldErrorMessage("lastName", code, t)).toBe(
                    messages["validation.lastRequired"],
                );
                expect(getFieldErrorMessage("other", code, t)).toBe(
                    messages["validation.required"],
                );
            }
        },
    );
});
