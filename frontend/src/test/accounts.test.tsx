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

function mount() {
    const store = createAppStore();
    store.dispatch(sessionReceived(user));
    render(
        <Provider store={store}>
            <MemoryRouter initialEntries={["/app"]}>
                <App />
            </MemoryRouter>
        </Provider>,
    );
}

describe("all accounts", () => {
    it("changes theme and language in personal settings", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            json({ items: [], page: 1, pageSize: 12, totalItems: 0, totalPages: 0 }),
        );
        mount();

        await userEvent.click(await screen.findByRole("link", { name: "Settings" }));
        expect(await screen.findByRole("heading", { name: "Settings" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { name: "Preferences" })).toBeInTheDocument();
        expect(screen.getByRole("heading", { name: "Currency" })).toBeInTheDocument();
        expect(screen.getByText("Appearance")).toBeInTheDocument();
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
        expect(screen.getByRole("link", { name: "All accounts" })).toHaveAttribute("href", "/app");
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
