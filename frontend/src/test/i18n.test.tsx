import { describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { createAppStore } from "@/app/store";
import { I18nProvider } from "@/shared/i18n/I18nProvider";
import i18n from "@/shared/i18n";
import { LanguageSelect } from "@/shared/ui/language-select";
import { pl } from "@/shared/i18n/pl";
import { LoginForm } from "@/features/auth/ui/LoginForm";
import { getApiError, getFieldErrorMessage } from "@/shared/api/apiError";

describe("internationalization", () => {
    it("switches visible text, validation, metadata, and persists the choice", async () => {
        localStorage.removeItem("locale");
        render(
            <Provider store={createAppStore()}>
                <I18nProvider>
                    <MemoryRouter>
                        <LanguageSelect />
                        <LoginForm onSuccess={vi.fn()} />
                    </MemoryRouter>
                </I18nProvider>
            </Provider>,
        );
        expect(screen.getByLabelText("Email address")).toBeInTheDocument();
        await userEvent.selectOptions(screen.getByLabelText("Language"), "pl");
        expect(screen.getByLabelText("Adres e-mail")).toBeInTheDocument();
        expect(i18n.t("home.welcome", { name: "Ala" })).toBe("Witaj, Ala");
        await userEvent.click(screen.getByRole("button", { name: "Zaloguj się" }));
        expect(screen.getByLabelText("Adres e-mail")).toHaveAccessibleDescription(
            "Podaj poprawny adres e-mail.",
        );
        const fetch = vi.spyOn(globalThis, "fetch").mockResolvedValue(
            new Response(
                JSON.stringify({
                    code: "INVALID_EMAIL_OR_PASSWORD",
                    message: "English backend text",
                }),
                { status: 400, headers: { "Content-Type": "application/json" } },
            ),
        );
        await userEvent.type(screen.getByLabelText("Adres e-mail"), "person@example.com");
        await userEvent.type(screen.getByLabelText("Hasło", { exact: true }), "password123");
        await userEvent.click(screen.getByRole("button", { name: "Zaloguj się" }));
        expect(
            await screen.findByText("Nieprawidłowy adres e-mail lub hasło."),
        ).toBeInTheDocument();
        expect((fetch.mock.calls[0][0] as Request).headers.get("Accept-Language")).toBe("pl");
        await waitFor(() => expect(document.documentElement.lang).toBe("pl"));
        expect(document.title).toContain("Finanse firmy");
        expect(localStorage.getItem("locale")).toBe("pl");
        localStorage.removeItem("locale");
    });

    it("uses error and field codes instead of server message wording", () => {
        const t = (key: keyof typeof pl) => pl[key];
        expect(
            getApiError(
                {
                    status: 400,
                    data: { code: "INVALID_EMAIL_OR_PASSWORD", message: "Wrong email or password" },
                },
                t,
            ).message,
        ).toBe("Nieprawidłowy adres e-mail lub hasło.");
        expect(getFieldErrorMessage("email", "VALIDATION_EMAIL", t)).toBe(
            "Podaj poprawny adres e-mail.",
        );
        expect(
            getApiError(
                { status: 400, data: { code: "SOMETHING_NEW", message: "Internal detail" } },
                t,
            ).message,
        ).toBe("Coś poszło nie tak. Spróbuj ponownie.");
    });
});
