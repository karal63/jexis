import { describe, expect, it, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router-dom";
import App from "@/app/App";
import { createAppStore } from "@/app/store";
import { sessionReceived } from "@/features/auth/model/sessionSlice";

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

function mount(path = "/app") {
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
        expect(screen.getByRole("link", { name: "All accounts" })).toHaveAttribute("href", "/app");
    });

    it("opens bank setup after onboarding when the payout account is required", async () => {
        vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const url = new URL((input as Request).url);
            if (url.pathname.endsWith("/accounts/account-1")) return json(accounts[0]);
            if (url.pathname.endsWith("/accounts/account-1/onboarding"))
                return json({
                    status: "COMPLETED",
                    canOnboard: true,
                    url: null,
                    externalAccountRequired: true,
                });
            return json({}, 404);
        });
        mount("/app/accounts/account-1");

        expect(
            await screen.findByRole("heading", { name: "Add a bank account" }),
        ).toBeInTheDocument();
        expect(screen.getByRole("textbox", { name: "Routing number" })).toBeDisabled();
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
