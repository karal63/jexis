import { loadConnectAndInitialize } from "@stripe/connect-js/pure";
import { loadStripe } from "@stripe/stripe-js/pure";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router-dom";
import App from "@/app/App";
import { createAppStore } from "@/app/store";
import { sessionReceived } from "@/features/auth/model/sessionSlice";
import i18n from "@/shared/i18n";

vi.mock("@stripe/stripe-js/pure", () => ({ loadStripe: vi.fn() }));
vi.mock("@stripe/connect-js/pure", () => ({ loadConnectAndInitialize: vi.fn() }));
vi.mock("@stripe/react-connect-js", () => ({
    ConnectComponentsProvider: ({ children }: { children: React.ReactNode }) => children,
    ConnectAccountManagement: () => <div aria-label="Stripe account management" role="region" />,
}));

const user = {
    id: "user-1",
    firstName: "Sam",
    lastName: "Test",
    email: "sam@example.com",
    phoneNumber: null,
    roles: ["USER"],
    isActivated: true,
    createdAt: "",
    updatedAt: "",
};

const accounts = [
    {
        id: "account-1",
        firstName: "North",
        lastName: "Studio",
        email: "north@example.com",
        city: null,
        country: null,
        owner: { id: "user-1" },
    },
    {
        id: "account-2",
        firstName: "West",
        lastName: "Works",
        email: "west@example.com",
        city: null,
        country: null,
        owner: { id: "someone-else" },
    },
];

const json = (data: unknown, status = 200) =>
    new Response(JSON.stringify(data), { status, headers: { "Content-Type": "application/json" } });

function mount(path = "/app/accounts") {
    const store = createAppStore();
    store.dispatch(sessionReceived(user));
    render(
        <Provider store={store}>
            <MemoryRouter initialEntries={[path]}>
                <App />
            </MemoryRouter>
        </Provider>,
    );
}

describe("app entry", () => {
    beforeEach(() => localStorage.removeItem("lastVisitedAccount:user-1"));

    it("shows the account list when several accounts exist and none was visited", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            json({ items: accounts, totalItems: 2, totalPages: 1 }),
        );
        mount("/app");
        expect(await screen.findByRole("heading", { name: "All accounts" })).toBeInTheDocument();
    });

    it("opens the only account directly and remembers it for the next visit", async () => {
        vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const pathname = new URL((input as Request).url).pathname;
            if (pathname.endsWith("/accounts/me"))
                return json({ items: [accounts[0]], totalItems: 1, totalPages: 1 });
            if (pathname.endsWith("/accounts/account-1/onboarding"))
                return json({ status: "COMPLETED", canOnboard: false });
            if (pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            return json({}, 404);
        });
        mount("/app");
        expect(await screen.findByRole("heading", { name: "North Studio" })).toBeInTheDocument();
        await waitFor(() =>
            expect(localStorage.getItem("lastVisitedAccount:user-1")).toBe("account-1"),
        );
    });

    it("opens the remembered account when several accounts exist", async () => {
        localStorage.setItem("lastVisitedAccount:user-1", "account-2");
        const fetch = vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const pathname = new URL((input as Request).url).pathname;
            if (pathname.endsWith("/accounts/account-2/onboarding"))
                return json({ status: "COMPLETED", canOnboard: false });
            if (pathname.endsWith("/accounts/account-2")) return json(accounts[1]);
            return json({}, 404);
        });
        mount("/app");
        expect(await screen.findByRole("heading", { name: "West Works" })).toBeInTheDocument();
        expect(
            fetch.mock.calls.some(([request]) =>
                new URL((request as Request).url).pathname.endsWith("/accounts/me"),
            ),
        ).toBe(false);
    });

    it("falls back to the list and clears an unavailable account", async () => {
        localStorage.setItem("lastVisitedAccount:user-1", "deleted-account");
        vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const pathname = new URL((input as Request).url).pathname;
            if (pathname.endsWith("/accounts/deleted-account")) return json({}, 404);
            if (pathname.endsWith("/accounts/me"))
                return json({ items: accounts, totalItems: 2, totalPages: 1 });
            return json({}, 404);
        });
        mount("/app");
        expect(await screen.findByRole("heading", { name: "All accounts" })).toBeInTheDocument();
        expect(localStorage.getItem("lastVisitedAccount:user-1")).toBeNull();
    });

    it("keeps the account list available when a choice is saved", async () => {
        localStorage.setItem("lastVisitedAccount:user-1", "account-1");
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            json({ items: accounts, totalItems: 2, totalPages: 1 }),
        );
        mount("/app/accounts");
        expect(await screen.findByRole("heading", { name: "All accounts" })).toBeInTheDocument();
    });
});

describe("all accounts", () => {
    it("creates an account once, opens it, and refreshes the account list", async () => {
        let finishCreation!: (response: Response) => void;
        let created = false;
        let onboardingStatus = "NOT_STARTED";
        const fetch = vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const request = input as Request;
            const url = new URL(request.url);
            if (url.pathname.endsWith("/accounts/create")) {
                return new Promise<Response>((resolve) => {
                    finishCreation = (response) => {
                        created = true;
                        resolve(response);
                    };
                });
            }
            if (url.pathname.endsWith("/accounts/account-1/onboarding")) {
                if (request.method === "POST") onboardingStatus = "PENDING_VERIFICATION";
                return json({ status: onboardingStatus, canOnboard: true, url: null });
            }
            if (url.pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            if (url.pathname.endsWith("/accounts/me"))
                return json({
                    items: created ? [accounts[0]] : [],
                    page: 1,
                    pageSize: 12,
                    totalItems: created ? 1 : 0,
                    totalPages: created ? 1 : 0,
                });
            return json({}, 404);
        });
        mount();
        const browser = userEvent.setup();
        await screen.findByRole("heading", { name: "No accounts yet" });
        await browser.click(screen.getByRole("button", { name: "Create account" }));
        expect(
            screen.getByRole("alertdialog", { name: "Create a new account?" }),
        ).toBeInTheDocument();
        expect(finishCreation).toBeUndefined();
        await browser.click(screen.getByRole("button", { name: "Create account" }));
        const pendingButton = screen.getByRole("button", { name: "Creating account…" });
        expect(pendingButton).toBeDisabled();
        expect(screen.getByRole("button", { name: "Cancel" })).toBeDisabled();
        await browser.keyboard("{Escape}");
        expect(screen.getByRole("alertdialog")).toBeInTheDocument();
        expect(screen.getByRole("status")).toHaveTextContent("Creating account…");
        await browser.click(pendingButton);
        await waitFor(() => expect(finishCreation).toBeTypeOf("function"));
        const requests = fetch.mock.calls
            .map(([input]) => input as Request)
            .filter((request) => new URL(request.url).pathname.endsWith("/accounts/create"));
        expect(requests).toHaveLength(1);
        expect(requests[0].method).toBe("POST");
        expect(requests[0].credentials).toBe("include");
        expect(await requests[0].clone().text()).toBe("");
        finishCreation(json(accounts[0]));

        expect(
            await screen.findByRole("heading", { name: "North Studio", level: 1 }),
        ).toBeInTheDocument();
        await browser.click(screen.getByRole("button", { name: "Start onboarding" }));
        expect(await screen.findByText("Verification in progress")).toBeInTheDocument();
        expect(
            screen.queryByRole("navigation", { name: "Account overview" }),
        ).not.toBeInTheDocument();
        expect(fetch).toHaveBeenCalledWith(
            expect.objectContaining({
                method: "POST",
                url: expect.stringContaining("/accounts/account-1/onboarding"),
            }),
        );
        await browser.click(screen.getByRole("link", { name: "All accounts" }));
        expect(await screen.findByRole("link", { name: "Open North Studio" })).toBeInTheDocument();
    });

    it("shows creation errors and lets the user retry", async () => {
        let attempts = 0;
        vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const url = new URL((input as Request).url);
            if (url.pathname.endsWith("/accounts/create")) {
                attempts++;
                return attempts === 1
                    ? json({ code: "USER_NOT_ACTIVATED" }, 403)
                    : json(accounts[0]);
            }
            if (url.pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            if (url.pathname.endsWith("/accounts/account-1/onboarding"))
                return json({ status: "NOT_STARTED", canOnboard: true, url: null });
            return json({ items: [], page: 1, pageSize: 12, totalItems: 0, totalPages: 0 });
        });
        mount();
        const browser = userEvent.setup();
        await screen.findByRole("heading", { name: "No accounts yet" });
        await browser.click(screen.getByRole("button", { name: "Create account" }));
        await browser.click(screen.getByRole("button", { name: "Create account" }));
        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Your account is not activated.",
        );
        expect(screen.getByRole("alertdialog")).toBeInTheDocument();
        await browser.click(screen.getByRole("button", { name: "Create account" }));
        expect(
            await screen.findByRole("heading", { name: "North Studio", level: 1 }),
        ).toBeInTheDocument();
        expect(attempts).toBe(2);
    });

    it("cancels or dismisses confirmation without creating an account", async () => {
        const fetch = vi
            .spyOn(globalThis, "fetch")
            .mockResolvedValue(
                json({ items: [], page: 1, pageSize: 12, totalItems: 0, totalPages: 0 }),
            );
        mount();
        const browser = userEvent.setup();
        await screen.findByRole("heading", { name: "No accounts yet" });
        const opener = screen.getByRole("button", { name: "Create account" });
        await browser.click(opener);
        expect(screen.getByRole("button", { name: "Cancel" })).toHaveFocus();
        await browser.click(screen.getByRole("button", { name: "Cancel" }));
        expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
        expect(opener).toHaveFocus();
        await browser.click(opener);
        await browser.keyboard("{Escape}");
        expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
        expect(opener).toHaveFocus();
        expect(fetch.mock.calls.some(([input]) => (input as Request).method === "POST")).toBe(
            false,
        );
    });

    it("confirms deletion, lets the owner retry a failure, and refreshes the account list", async () => {
        let deleted = false;
        let finishDeletion!: (response: Response) => void;
        const deleteRequest = vi.fn(
            () =>
                new Promise<Response>((resolve) => {
                    finishDeletion = resolve;
                }),
        );
        vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const request = input as Request;
            const { pathname } = new URL(request.url);
            if (pathname.endsWith("/accounts/account-1/delete") && request.method === "POST")
                return deleteRequest();
            if (pathname.endsWith("/accounts/me"))
                return json({
                    items: deleted ? [] : [accounts[0]],
                    page: 1,
                    pageSize: 12,
                    totalItems: deleted ? 0 : 1,
                    totalPages: deleted ? 0 : 1,
                });
            if (deleted) return json({ code: "ACCOUNT_NOT_FOUND" }, 404);
            if (pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            if (pathname.endsWith("/onboarding"))
                return json({ status: "COMPLETED", canOnboard: true, canDelete: true });
            return json({}, 404);
        });
        vi.stubEnv("VITE_STRIPE_PUBLISHABLE_KEY", "");
        try {
            mount();
            const browser = userEvent.setup();
            await browser.click(await screen.findByRole("link", { name: "Open North Studio" }));
            await browser.click(await screen.findByRole("button", { name: "Profile menu" }));
            await browser.click(screen.getByRole("link", { name: "Account settings" }));

            const trigger = await screen.findByRole("button", { name: "Delete account" });
            await browser.click(trigger);
            const dialog = within(
                screen.getByRole("alertdialog", { name: "Delete this business account?" }),
            );
            expect(dialog.getByText(/Deleting North Studio will close/)).toBeInTheDocument();
            expect(dialog.getByRole("button", { name: "Cancel" })).toHaveFocus();
            await browser.click(dialog.getByRole("button", { name: "Cancel" }));
            await waitFor(() => expect(trigger).toHaveFocus());
            expect(deleteRequest).not.toHaveBeenCalled();

            await browser.click(trigger);
            const confirmation = within(screen.getByRole("alertdialog"));
            await browser.click(confirmation.getByRole("button", { name: "Delete account" }));
            const pending = confirmation.getByRole("button", { name: "Deleting account…" });
            expect(pending).toBeDisabled();
            await browser.keyboard("{Escape}");
            expect(screen.getByRole("alertdialog")).toBeInTheDocument();
            await browser.click(pending);
            expect(deleteRequest).toHaveBeenCalledTimes(1);
            finishDeletion(json({ code: "FORBIDDEN" }, 403));
            expect(await confirmation.findByRole("alert")).toHaveTextContent(
                "You don’t have permission to do that.",
            );

            await browser.click(confirmation.getByRole("button", { name: "Delete account" }));
            await waitFor(() => expect(deleteRequest).toHaveBeenCalledTimes(2));
            deleted = true;
            finishDeletion(new Response("Account has been deleted."));
            expect(
                await screen.findByRole("heading", { name: "No accounts yet" }),
            ).toBeInTheDocument();
            expect(localStorage.getItem("lastVisitedAccount:user-1")).toBeNull();
        } finally {
            vi.unstubAllEnvs();
        }
    });

    it.each([
        ["NOT_STARTED", false, true],
        ["ACTION_REQUIRED", false, true],
        ["PENDING_VERIFICATION", false, true],
        ["RESTRICTED", false, true],
        ["COMPLETED", true, true],
        ["NOT_STARTED", false, false],
        ["COMPLETED", true, false],
    ])(
        "uses deletion permission in %s (bank setup %s, can delete %s)",
        async (status, externalAccountRequired, canDelete) => {
            vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
                const { pathname } = new URL((input as Request).url);
                if (pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
                if (pathname.endsWith("/onboarding"))
                    return json({ status, canOnboard: true, canDelete, externalAccountRequired });
                return json({}, 404);
            });
            mount(
                externalAccountRequired
                    ? "/app/accounts/account-1/external-account"
                    : "/app/accounts/account-1",
            );
            await screen.findByRole("heading", {
                name: externalAccountRequired ? "Add a bank account" : "North Studio",
            });
            expect(screen.queryByRole("button", { name: "Profile menu" })).not.toBeInTheDocument();
            if (canDelete) {
                expect(screen.getByRole("button", { name: "Delete account" })).toBeEnabled();
            } else {
                expect(
                    screen.queryByRole("button", { name: "Delete account" }),
                ).not.toBeInTheDocument();
            }
        },
    );

    it("changes theme and language in personal settings", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            json({ items: [], page: 1, pageSize: 12, totalItems: 0, totalPages: 0 }),
        );
        mount();

        await userEvent.click(await screen.findByRole("link", { name: "Settings" }));
        expect(await screen.findByRole("heading", { name: "Settings" })).toBeInTheDocument();
        await userEvent.click(screen.getByRole("tab", { name: "Appearance" }));
        expect(screen.getByRole("heading", { name: "Preferences" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { name: "Currency" })).toBeInTheDocument();
        expect(screen.getByText("Language")).toBeInTheDocument();
        expect(screen.getByText("USD")).toBeInTheDocument();

        await userEvent.selectOptions(screen.getByRole("combobox", { name: "Appearance" }), "dark");
        await waitFor(() => expect(document.documentElement.dataset.theme).toBe("dark"));
        expect(localStorage.getItem("theme")).toBe("dark");

        await userEvent.selectOptions(screen.getByRole("combobox", { name: "Language" }), "pl");
        expect(screen.getByRole("heading", { name: "Ustawienia" })).toBeInTheDocument();
        expect(document.documentElement.lang).toBe("pl");
        expect(localStorage.getItem("locale")).toBe("pl");
        expect(screen.getByText("USD")).toBeInTheDocument();

        await userEvent.selectOptions(screen.getByRole("combobox", { name: "Język" }), "en");
        await userEvent.selectOptions(
            screen.getByRole("combobox", { name: "Appearance" }),
            "system",
        );
    });

    it("loads memberships and opens the selected account", async () => {
        const fetch = vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const url = new URL((input as Request).url);
            if (url.pathname.endsWith("/accounts/me"))
                return json({
                    items: accounts,
                    page: 1,
                    pageSize: 12,
                    totalItems: 2,
                    totalPages: 1,
                });
            if (url.pathname.endsWith("/accounts/account-2")) return json(accounts[1]);
            if (url.pathname.endsWith("/accounts/account-2/onboarding"))
                return json({ status: "COMPLETED", canOnboard: false, url: null });
            return json({}, 404);
        });
        mount();

        expect(await screen.findByRole("link", { name: "Open North Studio" })).toBeInTheDocument();
        expect(screen.getByText("Sam Test")).toBeInTheDocument();
        expect(screen.getByText("sam@example.com")).toBeInTheDocument();
        const shared = screen.getByRole("link", { name: "Open West Works" });
        expect(screen.getByText("Owner")).toBeInTheDocument();
        expect(fetch).toHaveBeenCalledWith(
            expect.objectContaining({
                url: expect.stringContaining("/accounts/me?page=1&pageSize=12"),
            }),
        );

        await userEvent.click(shared);
        expect(
            await screen.findByRole("heading", { name: "West Works", level: 1 }),
        ).toBeInTheDocument();
        expect(screen.getByText("Account overview")).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", { name: "Switch account" }));
        expect(
            within(screen.getByRole("dialog", { name: "Switch account" })).getByRole("link", {
                name: "All accounts",
            }),
        ).toHaveAttribute("href", "/app/accounts");
    });

    it("switches between available accounts from the sidebar", async () => {
        vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const url = new URL((input as Request).url);
            if (url.pathname.endsWith("/accounts/me"))
                return json({
                    items: accounts,
                    page: 1,
                    pageSize: 12,
                    totalItems: 2,
                    totalPages: 1,
                });
            if (url.pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            if (url.pathname.endsWith("/accounts/account-2")) return json(accounts[1]);
            if (url.pathname.endsWith("/onboarding"))
                return json({ status: "COMPLETED", canOnboard: false, url: null });
            return json({}, 404);
        });
        mount("/app/accounts/account-1");

        await screen.findByRole("heading", { name: "North Studio" });
        await userEvent.click(screen.getByRole("button", { name: "Switch account" }));
        const menu = await screen.findByRole("dialog", { name: "Switch account" });
        await userEvent.click(await within(menu).findByRole("link", { name: "West Works" }));
        expect(await screen.findByRole("heading", { name: "West Works" })).toBeInTheDocument();
    });

    it("signs out from the profile menu", async () => {
        const fetch = vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const request = input as Request;
            const url = new URL(request.url);
            if (url.pathname.endsWith("/auth/logout")) return new Response(null, { status: 204 });
            if (url.pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            if (url.pathname.endsWith("/accounts/account-1/onboarding")) {
                return json({ status: "COMPLETED", canOnboard: false, url: null });
            }
            return json({}, 404);
        });
        mount("/app/accounts/account-1");
        const browser = userEvent.setup();
        await screen.findByRole("heading", { name: "North Studio" });
        await browser.click(screen.getByRole("button", { name: "Profile menu" }));
        await browser.click(
            within(screen.getByRole("dialog", { name: "Profile menu" })).getByRole("button", {
                name: "Sign out",
            }),
        );

        expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
        expect(fetch).toHaveBeenCalledWith(
            expect.objectContaining({
                method: "POST",
                url: expect.stringContaining("/auth/logout"),
                credentials: "include",
            }),
        );
    });

    it("shows logout errors in the profile menu and keeps the session", async () => {
        vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const url = new URL((input as Request).url);
            if (url.pathname.endsWith("/auth/logout")) return json({}, 503);
            if (url.pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            if (url.pathname.endsWith("/accounts/account-1/onboarding")) {
                return json({ status: "COMPLETED", canOnboard: false, url: null });
            }
            return json({}, 404);
        });
        mount("/app/accounts/account-1");
        const browser = userEvent.setup();
        await screen.findByRole("heading", { name: "North Studio" });
        await browser.click(screen.getByRole("button", { name: "Profile menu" }));
        const menu = screen.getByRole("dialog", { name: "Profile menu" });
        await browser.click(within(menu).getByRole("button", { name: "Sign out" }));

        expect(await within(menu).findByRole("alert")).toHaveTextContent("Something went wrong");
        expect(within(menu).getByRole("button", { name: "Sign out" })).toBeEnabled();
        expect(screen.getByRole("heading", { name: "North Studio" })).toBeInTheDocument();
    });

    it("changes theme from the profile menu and returns from user settings", async () => {
        vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const url = new URL((input as Request).url);
            if (url.pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            if (url.pathname.endsWith("/accounts/account-1/onboarding")) {
                return json({ status: "COMPLETED", canOnboard: true, url: null });
            }
            if (url.pathname.endsWith("/accounts/me")) {
                return json({
                    items: accounts,
                    page: 1,
                    pageSize: 12,
                    totalItems: 2,
                    totalPages: 1,
                });
            }
            return json({}, 404);
        });
        try {
            mount("/app/accounts/account-1");
            const browser = userEvent.setup();
            await screen.findByRole("heading", { name: "North Studio" });
            await browser.click(screen.getByRole("button", { name: "Profile menu" }));
            const menu = screen.getByRole("dialog", { name: "Profile menu" });
            expect(within(menu).getAllByRole("separator")).toHaveLength(4);
            const themeButton = within(menu).getByRole("button", { name: "Theme" });
            expect(themeButton).toHaveAttribute("aria-expanded", "false");
            expect(within(menu).queryByRole("group", { name: "Theme" })).toBeNull();
            await browser.hover(themeButton);
            const themes = within(menu).getByRole("group", { name: "Theme" });
            expect(within(themes).getByRole("button", { name: "System" })).toHaveAttribute(
                "aria-pressed",
                "true",
            );
            await browser.click(within(themes).getByRole("button", { name: "Dark" }));
            await waitFor(() => expect(document.documentElement.dataset.theme).toBe("dark"));
            await browser.hover(themeButton);
            const selectedDark = within(
                within(menu).getByRole("group", { name: "Theme" }),
            ).getByRole("button", { name: "Dark" });
            expect(selectedDark).toHaveAttribute("aria-pressed", "true");
            expect(selectedDark.querySelector(".lucide-check")).toBeInTheDocument();
            await browser.unhover(themeButton);
            await waitFor(() =>
                expect(within(menu).queryByRole("group", { name: "Theme" })).toBeNull(),
            );
            expect(within(menu).getByRole("link", { name: "Account settings" })).toHaveAttribute(
                "href",
                "/app/accounts/account-1/settings",
            );
            await browser.click(within(menu).getByRole("link", { name: "User settings" }));
            expect(await screen.findByRole("heading", { name: "Settings" })).toBeInTheDocument();
            await browser.click(screen.getByRole("link", { name: "Back" }));
            expect(
                await screen.findByRole("heading", { name: "North Studio" }),
            ).toBeInTheDocument();
            await browser.click(screen.getByRole("button", { name: "Profile menu" }));
            await browser.click(
                within(screen.getByRole("dialog", { name: "Profile menu" })).getByRole("link", {
                    name: "All accounts",
                }),
            );
            expect(
                await screen.findByRole("heading", { name: "All accounts" }),
            ).toBeInTheDocument();
        } finally {
            localStorage.removeItem("theme");
        }
    });

    it("opens account settings from the profile menu and loads Stripe immediately", async () => {
        vi.stubEnv("VITE_STRIPE_PUBLISHABLE_KEY", "pk_test_example");
        await i18n.changeLanguage("en");
        document.documentElement.style.setProperty("--background", "#fafaf9");
        document.documentElement.style.setProperty("--card", "#ffffff");
        document.documentElement.style.setProperty("--border", "#123456");
        document.documentElement.style.setProperty("--ring", "#654321");
        const initialize = vi.mocked(loadConnectAndInitialize);
        const update = vi.fn();
        initialize.mockImplementation(({ fetchClientSecret }) => {
            void fetchClientSecret();
            return { update } as unknown as ReturnType<typeof loadConnectAndInitialize>;
        });
        const fetch = vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const url = new URL((input as Request).url);
            if (url.pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            if (url.pathname.endsWith("/onboarding"))
                return json({
                    status: "COMPLETED",
                    canOnboard: true,
                    url: null,
                    externalAccountRequired: false,
                });
            if (url.pathname.endsWith("/accounts/account-1/bank-account-session"))
                return json({ clientSecret: "secret" });
            return json({}, 404);
        });
        try {
            mount("/app/accounts/account-1");
            const browser = userEvent.setup();
            const profile = await screen.findByRole("button", { name: "Profile menu" });
            const navbar = within(screen.getByRole("banner"));
            const sidebar = screen.getByRole("navigation", { name: "Account overview" });
            expect(screen.queryByRole("link", { name: "Manage bank accounts" })).toBeNull();
            await browser.click(profile);
            const menu = screen.getByRole("dialog", { name: "Profile menu" });
            expect(within(menu).getByRole("button", { name: "Subscription" })).toBeDisabled();
            expect(initialize).not.toHaveBeenCalled();
            await browser.keyboard("{Escape}");
            expect(profile).toHaveFocus();
            await browser.click(profile);
            await browser.click(screen.getByRole("link", { name: "Account settings" }));

            expect(await screen.findByRole("heading", { name: "Settings" })).toBeInTheDocument();
            expect(
                await screen.findByRole("region", { name: "Stripe account management" }),
            ).toBeInTheDocument();
            await waitFor(() =>
                expect(fetch).toHaveBeenCalledWith(
                    expect.objectContaining({
                        method: "POST",
                        url: expect.stringContaining(
                            "/accounts/account-1/bank-account-session?manage=true",
                        ),
                    }),
                ),
            );
            expect(initialize).toHaveBeenCalledTimes(1);
            expect(initialize).toHaveBeenCalledWith(
                expect.objectContaining({
                    locale: "en-US",
                    appearance: {
                        variables: expect.objectContaining({
                            colorBackground: "#fafaf9",
                            formBackgroundColor: "#ffffff",
                            colorBorder: "#123456",
                            formHighlightColorBorder: "#654321",
                        }),
                    },
                }),
            );
            expect(screen.queryByRole("button", { name: "Manage bank accounts" })).toBeNull();
            expect(screen.getByRole("navigation", { name: "Account overview" })).toBe(sidebar);
            expect(navbar.getByRole("button", { name: "Profile menu" })).toBe(profile);
            expect(navbar.getByRole("button", { name: "Toggle sidebar" })).toBeInTheDocument();
            expect(navbar.getByText("Settings")).toBeInTheDocument();
            expect(screen.queryByRole("dialog", { name: "Profile menu" })).toBeNull();
            await i18n.changeLanguage("pl");
            await waitFor(() => expect(update).toHaveBeenCalledWith({ locale: "pl-PL" }));
            expect(initialize).toHaveBeenCalledTimes(1);
            document.documentElement.style.setProperty("--background", "#0f0f10");
            document.documentElement.style.setProperty("--card", "#151516");
            document.documentElement.style.setProperty("--border", "#abcdef");
            document.documentElement.dataset.theme = "dark";
            await waitFor(() =>
                expect(update).toHaveBeenCalledWith({
                    appearance: {
                        variables: expect.objectContaining({
                            colorBackground: "#0f0f10",
                            formBackgroundColor: "#151516",
                            colorBorder: "#abcdef",
                        }),
                    },
                }),
            );
        } finally {
            await i18n.changeLanguage("en");
            document.documentElement.style.removeProperty("--background");
            document.documentElement.style.removeProperty("--card");
            document.documentElement.style.removeProperty("--border");
            document.documentElement.style.removeProperty("--ring");
            initialize.mockReset();
            vi.unstubAllEnvs();
        }
    });

    it("redirects old bank management links to account settings with permission checks", async () => {
        vi.stubEnv("VITE_STRIPE_PUBLISHABLE_KEY", "pk_test_example");
        const fetch = vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const url = new URL((input as Request).url);
            if (url.pathname.endsWith("/accounts/account-1/onboarding")) {
                return json({ status: "COMPLETED", canOnboard: false, url: null });
            }
            return json({}, 404);
        });
        try {
            mount("/app/accounts/account-1/bank-accounts");
            expect(
                await screen.findByText("Ask an account owner or admin to manage Stripe settings."),
            ).toBeInTheDocument();
            expect(
                screen.getByRole("navigation", { name: "Account overview" }),
            ).toBeInTheDocument();
            expect(fetch).not.toHaveBeenCalledWith(
                expect.objectContaining({ url: expect.stringContaining("/bank-account-session") }),
            );
        } finally {
            vi.unstubAllEnvs();
        }
    });

    it("attaches a confirmed bank before leaving setup", async () => {
        vi.stubEnv("VITE_STRIPE_PUBLISHABLE_KEY", "pk_test_example");
        const collect = vi
            .fn()
            .mockResolvedValueOnce({
                error: { message: "Bank linking is unavailable for this account." },
            })
            .mockResolvedValueOnce({ setupIntent: { status: "requires_payment_method" } })
            .mockResolvedValueOnce({
                setupIntent: { id: "seti_test", status: "requires_confirmation" },
            });
        vi.mocked(loadStripe).mockResolvedValue({
            collectBankAccountForSetup: collect,
            confirmUsBankAccountSetup: vi.fn().mockResolvedValue({
                setupIntent: { id: "seti_test", status: "succeeded" },
            }),
        } as unknown as Awaited<ReturnType<typeof loadStripe>>);
        let externalAccountRequired = true;
        vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const url = new URL((input as Request).url);
            if (url.pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            if (url.pathname.endsWith("/onboarding"))
                return json({
                    status: "COMPLETED",
                    canOnboard: true,
                    url: null,
                    externalAccountRequired,
                });
            if (url.pathname.endsWith("/payout-bank-setup"))
                return json({ clientSecret: "secret", name: "Test Company" });
            if (url.pathname.endsWith("/payout-bank-setup/seti_test/complete")) {
                externalAccountRequired = false;
                return json({});
            }
            return json({}, 404);
        });
        try {
            mount("/app/accounts/account-1");
            const button = await screen.findByRole("button", { name: "Connect bank account" });
            await userEvent.click(button);
            expect(await screen.findByRole("alert")).toHaveTextContent(
                "Connecting bank — Bank linking is unavailable for this account.",
            );
            await userEvent.click(screen.getByRole("button", { name: "Try again" }));
            await waitFor(() => expect(button).toBeEnabled());
            expect(externalAccountRequired).toBe(true);
            await userEvent.click(button);
            await waitFor(() =>
                expect(screen.getByRole("button", { name: "Profile menu" })).toBeInTheDocument(),
            );
        } finally {
            vi.unstubAllEnvs();
        }
    });

    it("searches all memberships through the server and paginates results", async () => {
        const requests: string[] = [];
        vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const url = new URL((input as Request).url);
            requests.push(url.search);
            const search = url.searchParams.get("search");
            const page = Number(url.searchParams.get("page"));
            if (search === "West")
                return json({
                    items: [accounts[1]],
                    page: 1,
                    pageSize: 12,
                    totalItems: 1,
                    totalPages: 1,
                });
            return json({
                items: [accounts[page - 1]],
                page,
                pageSize: 12,
                totalItems: 13,
                totalPages: 2,
            });
        });
        mount();
        const browser = userEvent.setup();

        await screen.findByRole("link", { name: "Open North Studio" });
        await browser.click(screen.getByRole("button", { name: "Next" }));
        expect(await screen.findByRole("link", { name: "Open West Works" })).toBeInTheDocument();
        expect(requests.some((query) => query.includes("page=2"))).toBe(true);

        await browser.type(screen.getByRole("searchbox", { name: "Search accounts" }), "West");
        await waitFor(() =>
            expect(
                requests.some((query) => query.includes("search=West") && query.includes("page=1")),
            ).toBe(true),
        );
        await waitFor(() =>
            expect(
                screen.queryByRole("navigation", { name: "Account pages" }),
            ).not.toBeInTheDocument(),
        );
    });

    it("shows an empty state when the user has no accounts", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            json({ items: [], page: 1, pageSize: 12, totalItems: 0, totalPages: 0 }),
        );
        mount();
        expect(await screen.findByRole("heading", { name: "No accounts yet" })).toBeInTheDocument();
    });
});
