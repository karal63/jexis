import { authApi } from "@/features/auth/api/authApi";
import { profileApi } from "@/features/profile/api/profileApi";
import { sessionEnded } from "@/shared/api/sessionEvents";
import { describe, expect, it, vi } from "vitest";
import { act, render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router-dom";
import App from "@/app/App";
import { createAppStore } from "@/app/store";
import { sessionReceived } from "@/features/auth/model/sessionSlice";

const accountUser = {
    id: "user-1",
    firstName: "Sam",
    lastName: "Taylor",
    email: "sam@example.com",
    phoneNumber: "+48 512 345 678",
    roles: ["USER"],
    isActivated: true,
    createdAt: "",
    updatedAt: "",
};

function mount(user = accountUser) {
    const store = createAppStore();
    store.dispatch(sessionReceived(user));
    render(
        <Provider store={store}>
            <MemoryRouter initialEntries={["/app/settings"]}>
                <App />
            </MemoryRouter>
        </Provider>,
    );
    return store;
}

describe("settings", () => {
    it("resends the activation email and shares feedback with the account banner", async () => {
        const fetch = vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const request = input as Request;
            if (request.url.endsWith("/me/activation-email")) {
                return new Response(null, { status: 204 });
            }
            return new Response("Not found", { status: 404 });
        });
        const store = mount({ ...accountUser, isActivated: false });
        const browser = userEvent.setup();

        await browser.click(await screen.findByRole("button", { name: "Send activation email" }));
        const confirmation = "Activation email sent. Check your inbox for the latest link.";
        expect(await screen.findByText(confirmation)).toBeInTheDocument();
        const request = fetch.mock.calls[0][0] as Request;
        expect(request.url).toMatch(/\/me\/activation-email$/);
        expect(request.method).toBe("POST");

        await browser.click(screen.getAllByRole("link", { name: "All accounts" })[0]);
        await screen.findByRole("heading", { name: "All accounts" });
        expect(screen.getByText(confirmation)).toBeInTheDocument();

        act(() => store.dispatch(sessionReceived(accountUser)));
        expect(screen.queryByRole("button", { name: "Send activation email" })).toBeNull();
    });

    it("switches between available tabs and leaves future sections disabled", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response("Not found", { status: 404 }));
        mount();
        const browser = userEvent.setup();
        const profile = await screen.findByRole("tab", { name: "Public profile" });
        const appearance = screen.getByRole("tab", { name: "Appearance" });
        const password = screen.getByRole("tab", { name: "Password & authentication" });
        expect(profile).toHaveAttribute("aria-selected", "true");
        expect(screen.getByRole("heading", { name: "Personal info" })).toBeInTheDocument();
        expect(screen.queryByRole("heading", { name: "Preferences" })).not.toBeInTheDocument();
        expect(screen.getByRole("button", { name: "Billing" })).toBeDisabled();
        expect(screen.getByRole("button", { name: "Sessions" })).toBeDisabled();

        await browser.click(appearance);
        expect(appearance).toHaveAttribute("aria-selected", "true");
        expect(screen.getByRole("heading", { name: "Currency" })).toBeInTheDocument();
        await browser.keyboard("{ArrowRight}");
        expect(password).toHaveAttribute("aria-selected", "true");
        expect(password).toHaveFocus();
        expect(screen.getByRole("heading", { name: "Change password" })).toBeInTheDocument();
        await browser.keyboard("{Home}");
        expect(profile).toHaveAttribute("aria-selected", "true");
    });

    it("navigates in rendered tab order with wraparound", async () => {
        mount();
        const browser = userEvent.setup();
        const tabs = await screen.findAllByRole("tab");
        await browser.click(tabs[0]);
        for (let i = 1; i <= tabs.length; i++) {
            await browser.keyboard("{ArrowRight}");
            expect(tabs[i % tabs.length]).toHaveFocus();
            expect(tabs[i % tabs.length]).toHaveAttribute("aria-selected", "true");
        }
        await browser.keyboard("{End}");
        expect(tabs.at(-1)).toHaveFocus();
        await browser.keyboard("{Home}");
        expect(tabs[0]).toHaveFocus();
        await browser.keyboard("{ArrowLeft}");
        expect(tabs.at(-1)).toHaveFocus();
    });

    it("validates and saves personal information, including clearing the phone number", async () => {
        const fetch = vi.spyOn(globalThis, "fetch").mockImplementation(async (input) => {
            const request = input as Request;
            if (request.url.endsWith("/users/user-1/edit")) {
                return new Response(
                    JSON.stringify({ ...accountUser, firstName: "Alex", phoneNumber: null }),
                    {
                        headers: { "Content-Type": "application/json" },
                    },
                );
            }
            return new Response("Not found", { status: 404 });
        });
        const store = mount();
        const browser = userEvent.setup();

        const firstName = await screen.findByRole("textbox", { name: "First name" });
        expect(firstName).toHaveValue("Sam");
        expect(screen.getByRole("textbox", { name: "Phone number" })).toHaveValue(
            "+48 512 345 678",
        );

        await browser.clear(firstName);
        await browser.click(screen.getByRole("button", { name: "Save changes" }));
        expect(await screen.findByText("First name is required.")).toBeInTheDocument();
        expect(fetch).not.toHaveBeenCalled();

        await browser.type(firstName, "Alex");
        await browser.clear(screen.getByRole("textbox", { name: "Phone number" }));
        await browser.click(screen.getByRole("button", { name: "Save changes" }));

        expect(await screen.findByRole("status")).toHaveTextContent("Changes saved.");
        const request = fetch.mock.calls[0][0] as Request;
        expect(request.method).toBe("PATCH");
        expect(request.url).toContain("/users/user-1/edit");
        await expect(request.json()).resolves.toEqual({
            firstName: "Alex",
            lastName: "Taylor",
            phoneNumber: null,
        });
        expect(store.getState().session.user?.firstName).toBe("Alex");
        expect(store.getState().session.user?.phoneNumber).toBeNull();
    });

    it("shows the email in a reusable alert after the reset request succeeds", async () => {
        const fetch = vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response("Sent"));
        mount();
        const browser = userEvent.setup();
        await browser.click(await screen.findByRole("tab", { name: "Password & authentication" }));
        await browser.click(await screen.findByRole("button", { name: "Send reset link" }));

        const alert = await screen.findByRole("alertdialog");
        expect(
            within(alert).getByText("A password reset link was sent to sam@example.com."),
        ).toBeInTheDocument();
        const request = fetch.mock.calls[0][0] as Request;
        expect(request.method).toBe("POST");
        expect(request.url).toContain("/users/change-password");
        expect(await request.text()).toBe("");

        await browser.keyboard("{Escape}");
        await waitFor(() => expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument());
        expect(screen.getByRole("button", { name: "Send reset link" })).toHaveFocus();
    });

    it("shows a duplicate phone error beside the phone field", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue(
            new Response(JSON.stringify({ code: "PHONE_NUMBER_EXISTS" }), {
                status: 409,
                headers: { "Content-Type": "application/json" },
            }),
        );
        mount();
        const browser = userEvent.setup();
        const phone = await screen.findByRole("textbox", { name: "Phone number" });
        await browser.clear(phone);
        await browser.type(phone, "+48 513 345 678");
        await browser.click(screen.getByRole("button", { name: "Save changes" }));
        expect(await screen.findByText("This phone number is already in use.")).toBeInTheDocument();
        expect(phone).toHaveAttribute("aria-invalid", "true");
    });

    it("shows an error without claiming a link was sent when the request fails", async () => {
        vi.spyOn(globalThis, "fetch").mockResolvedValue(new Response("Error", { status: 500 }));
        mount();
        await userEvent.click(
            await screen.findByRole("tab", { name: "Password & authentication" }),
        );
        await userEvent.click(await screen.findByRole("button", { name: "Send reset link" }));
        expect(await screen.findByRole("alert")).toHaveTextContent("Something went wrong");
        expect(screen.queryByRole("alertdialog")).not.toBeInTheDocument();
    });
});

function deferredResponse() {
    let resolve!: (value: Response) => void;
    const promise = new Promise<Response>((done) => {
        resolve = done;
    });
    return { promise, resolve };
}
describe("settings reliability", () => {
    it("keeps drafts across tabs and same-user session updates, but resets for another account", async () => {
        const store = mount();
        const browser = userEvent.setup();
        const first = await screen.findByRole("textbox", { name: "First name" });
        await browser.clear(first);
        await browser.type(first, "Draft");
        await browser.click(screen.getByRole("tab", { name: "Appearance" }));
        act(() => {
            store.dispatch(
                sessionReceived({ ...accountUser, firstName: "Server", lastName: "Updated" }),
            );
        });
        await browser.click(screen.getByRole("tab", { name: "Public profile" }));
        expect(first).toHaveValue("Draft");
        expect(screen.getByRole("textbox", { name: "Last name" })).toHaveValue("Updated");
        act(() => {
            store.dispatch(sessionReceived({ ...accountUser, id: "another", firstName: "Other" }));
        });
        expect(screen.getByRole("textbox", { name: "First name" })).toHaveValue("Other");
    });
    it("disables editing during a delayed save and synchronizes the canonical response", async () => {
        const pending = deferredResponse();
        vi.spyOn(globalThis, "fetch").mockReturnValue(pending.promise);
        const store = mount();
        const browser = userEvent.setup();
        const first = await screen.findByRole("textbox", { name: "First name" });
        await browser.clear(first);
        await browser.type(first, "Alex");
        await browser.click(screen.getByRole("button", { name: "Save changes" }));
        await waitFor(() => expect(first).toBeDisabled());
        expect(screen.getByRole("textbox", { name: "Phone number" })).toBeDisabled();
        await act(async () =>
            pending.resolve(
                new Response(
                    JSON.stringify({
                        ...accountUser,
                        firstName: "Alex",
                        phoneNumber: "+48512345678",
                    }),
                ),
            ),
        );
        expect(await screen.findByRole("status")).toHaveTextContent("Changes saved.");
        expect(first).toBeEnabled();
        expect(store.getState().session.user?.firstName).toBe("Alex");
        await waitFor(() =>
            expect(authApi.endpoints.currentUser.select()(store.getState()).data?.firstName).toBe(
                "Alex",
            ),
        );
    });
    it.each(["logout", "switch", "same-account-login"])(
        "ignores late profile responses after %s",
        async (action) => {
            const pending = deferredResponse();
            vi.spyOn(globalThis, "fetch").mockReturnValue(pending.promise);
            const store = createAppStore();
            store.dispatch(sessionReceived(accountUser));
            const request = store.dispatch(
                profileApi.endpoints.updatePersonalInfo.initiate({
                    id: accountUser.id,
                    firstName: "Late",
                    lastName: "Taylor",
                    phoneNumber: null,
                }),
            );
            await waitFor(() => expect(globalThis.fetch).toHaveBeenCalled());
            store.dispatch(sessionEnded());
            if (action !== "logout")
                store.dispatch(
                    sessionReceived({
                        ...accountUser,
                        id: action === "switch" ? "other" : accountUser.id,
                    }),
                );
            pending.resolve(new Response(JSON.stringify({ ...accountUser, firstName: "Late" })));
            await request;
            expect(store.getState().session.user?.firstName).not.toBe("Late");
            expect(
                authApi.endpoints.currentUser.select()(store.getState()).data?.firstName,
            ).not.toBe("Late");
        },
    );
});
