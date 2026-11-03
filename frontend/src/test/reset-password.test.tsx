import { describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Provider } from "react-redux";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { createAppStore } from "@/app/store";
import ResetPasswordPage from "@/pages/auth/ResetPasswordPage";

function mount(path = "/reset-password?token=email-token") {
    render(
        <Provider store={createAppStore()}>
            <MemoryRouter initialEntries={[path]}>
                <Routes>
                    <Route path="/reset-password" element={<ResetPasswordPage />} />
                </Routes>
            </MemoryRouter>
        </Provider>,
    );
}

describe("reset password", () => {
    it("requires a token from the email link", () => {
        mount("/reset-password");
        expect(screen.getByText("Reset link missing")).toBeInTheDocument();
        expect(screen.queryByLabelText("New password")).not.toBeInTheDocument();
    });

    it("validates passwords and sends the token with only the new password", async () => {
        const fetch = vi
            .spyOn(globalThis, "fetch")
            .mockResolvedValue(new Response("Password changed successfully."));
        mount();
        const browser = userEvent.setup();
        await browser.type(screen.getByLabelText("New password"), "short");
        await browser.type(screen.getByLabelText("Confirm new password"), "short");
        await browser.click(screen.getByRole("button", { name: /reset password/i }));
        expect(screen.getByLabelText("New password")).toHaveAccessibleDescription(
            "Password must be at least 8 characters.",
        );
        expect(fetch).not.toHaveBeenCalled();

        await browser.clear(screen.getByLabelText("New password"));
        await browser.type(screen.getByLabelText("New password"), "new-password-123");
        await browser.clear(screen.getByLabelText("Confirm new password"));
        await browser.type(screen.getByLabelText("Confirm new password"), "new-password-123");
        await browser.click(screen.getByRole("button", { name: /reset password/i }));

        expect(await screen.findByText("Password updated")).toBeInTheDocument();
        expect(fetch).toHaveBeenCalledOnce();
        const request = fetch.mock.calls[0][0] as Request;
        expect(request.url).toContain("/users/reset-password/confirm");
        expect(request.method).toBe("POST");
        await expect(request.json()).resolves.toEqual({
            token: "email-token",
            password: "new-password-123",
        });
    });

    it("offers a new link when the token has expired", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            new Response(JSON.stringify({ code: "INVALID_PASSWORD_RESET_TOKEN" }), {
                status: 400,
                headers: { "Content-Type": "application/json" },
            }),
        );
        mount();
        const browser = userEvent.setup();
        await browser.type(screen.getByLabelText("New password"), "new-password-123");
        await browser.type(screen.getByLabelText("Confirm new password"), "new-password-123");
        await browser.click(screen.getByRole("button", { name: /reset password/i }));
        await waitFor(() =>
            expect(screen.getByRole("alert")).toHaveTextContent("invalid or has expired"),
        );
        expect(screen.getByRole("link", { name: "Request one" })).toHaveAttribute(
            "href",
            "/forgot-password",
        );
    });
});
