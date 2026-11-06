import { describe, expect, it } from "vitest";
import { createProfileFields, normalizePhone } from "@/shared/forms/profileFields";
import { en } from "@/shared/i18n/en";
import { createPersonalInfoSchema } from "@/features/profile/model/personalInfo.schema";
import { createSignupSchema } from "@/features/auth/model/signup.schema";
const t = (key: keyof typeof en) => en[key];
describe("shared profile fields", () => {
    it.each(["+48 512 345 678", "+48 (512) 345-678", "+48512345678"])("normalizes %s", (value) =>
        expect(normalizePhone(value)).toBe("+48512345678"),
    );
    it.each(["512345678", "---", "+48", "+48 512 345 678 ext 2", "+" + "1".repeat(64)])(
        "rejects %s",
        (value) => expect(() => normalizePhone(value)).toThrow(),
    );
    it("allows absent phones and trims names", () => {
        expect(normalizePhone("   ")).toBeNull();
        expect(createProfileFields(t).firstName.parse(" Alex ")).toBe("Alex");
        expect(createProfileFields(t).firstName.safeParse("   ").success).toBe(false);
        expect(createProfileFields(t).lastName.safeParse("x".repeat(101)).success).toBe(false);
    });
    it("uses the same phone rule for signup and settings", () => {
        const profile = { firstName: "Alex", lastName: "Taylor", phoneNumber: "512345678" };
        expect(createPersonalInfoSchema(t).safeParse(profile).success).toBe(false);
        expect(
            createSignupSchema(t).safeParse({
                ...profile,
                email: "alex@example.com",
                password: "password-123",
                confirmPassword: "password-123",
            }).success,
        ).toBe(false);
    });
});
