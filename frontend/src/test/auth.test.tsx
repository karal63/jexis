import { describe, it, expect, vi } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Provider } from "react-redux";
import { MemoryRouter } from "react-router-dom";
import { createAppStore } from "@/app/store";
import { LoginForm } from "@/features/auth/ui/LoginForm";
import { authApi } from "@/features/auth/api/authApi";
import { sessionReceived } from "@/features/auth/model/sessionSlice";
import { sessionEnded } from "@/shared/api/sessionEvents";
import { signupSchema, toSignupRequest } from "@/features/auth/model/signup.schema";
import { baseApi } from "@/shared/api/baseApi";
import App from "@/app/App";
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
const json = (data: unknown, status = 200) =>
    new Response(JSON.stringify(data), { status, headers: { "Content-Type": "application/json" } });
function mockFetch(handler: (request: Request) => Response | Promise<Response>) {
    return vi
        .spyOn(globalThis, "fetch")
        .mockImplementation((input) => Promise.resolve(handler(input as Request)));
}
function mountForm(onSuccess = vi.fn()) {
    const store = createAppStore();
    render(
        <Provider store={store}>
            <MemoryRouter>
                <LoginForm onSuccess={onSuccess} />
            </MemoryRouter>
        </Provider>,
    );
    return { store, onSuccess };
}
async function fillLogin() {
    const browser = userEvent.setup();
    await browser.type(screen.getByLabelText("Email address"), user.email);
    await browser.type(screen.getByLabelText("Password", { exact: true }), "password123");
    await browser.click(screen.getByRole("button", { name: /sign in/i }));
}
describe("auth forms", () => {
    it("associates validation errors with fields and sends no invalid request", async () => {
        const fetch = mockFetch(() => json(user));
        mountForm();
        await userEvent.click(screen.getByRole("button", { name: /sign in/i }));
        const email = screen.getByLabelText("Email address");
        await waitFor(() => expect(email).toHaveAttribute("aria-invalid", "true"));
        expect(email).toHaveAccessibleDescription("Enter a valid email address.");
        expect(fetch).not.toHaveBeenCalled();
    });
    it("maps server field errors back to the form", async () => {
        mockFetch(() =>
            json(
                {
                    code: "VALIDATION_ERROR",
                    errors: [{ field: "email", message: "Email is unavailable." }],
                },
                400,
            ),
        );
        mountForm();
        await fillLogin();
        await waitFor(() =>
            expect(screen.getByLabelText("Email address")).toHaveAccessibleDescription(
                "Email is unavailable.",
            ),
        );
    });
    it("reports a failed login without attempting token refresh", async () => {
        const fetch = mockFetch(() => json({ message: "Wrong email or password" }, 401));
        const { onSuccess } = mountForm();
        await fillLogin();
        expect(await screen.findByRole("alert")).toHaveTextContent("Wrong email or password");
        expect(fetch).toHaveBeenCalledTimes(1);
        expect(onSuccess).not.toHaveBeenCalled();
    });
    it("stores the authenticated user and calls the page after success", async () => {
        mockFetch(() => json(user));
        const { store, onSuccess } = mountForm();
        await fillLogin();
        await waitFor(() => expect(onSuccess).toHaveBeenCalledOnce());
        expect(store.getState().session.user?.id).toBe(user.id);
    });
    it("validates signup rules and omits confirmation from the API payload", () => {
        const values = {
            firstName: " Sam ",
            lastName: "Test",
            email: user.email,
            phoneNumber: "",
            password: "password123",
            confirmPassword: "password123",
        };
        expect(signupSchema.safeParse({ ...values, firstName: "x".repeat(101) }).success).toBe(
            false,
        );
        expect(signupSchema.safeParse({ ...values, phoneNumber: "abc" }).success).toBe(false);
        expect(signupSchema.safeParse({ ...values, confirmPassword: "different" }).success).toBe(
            false,
        );
        expect(toSignupRequest(signupSchema.parse(values))).toEqual({
            firstName: "Sam",
            lastName: "Test",
            email: user.email,
            phoneNumber: null,
            password: "password123",
        });
    });
});
describe("session lifecycle", () => {
    it("restores the session on reload and logs out through the server", async () => {
        mockFetch((request) =>
            request.url.endsWith("/logout") ? new Response(null, { status: 204 }) : json(user),
        );
        const store = createAppStore();
        render(
            <Provider store={store}>
                <MemoryRouter initialEntries={["/app"]}>
                    <App />
                </MemoryRouter>
            </Provider>,
        );
        expect(await screen.findByRole("heading", { name: "Welcome, Sam" })).toBeInTheDocument();
        await userEvent.click(screen.getByRole("button", { name: "Sign out" }));
        expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
        expect(store.getState().session.user).toBeNull();
    });
    it("keeps the user signed in when server logout fails", async () => {
        mockFetch((request) => (request.url.endsWith("/logout") ? json({}, 503) : json(user)));
        const store = createAppStore();
        render(
            <Provider store={store}>
                <MemoryRouter initialEntries={["/app"]}>
                    <App />
                </MemoryRouter>
            </Provider>,
        );
        await screen.findByRole("heading", { name: "Welcome, Sam" });
        await userEvent.click(screen.getByRole("button", { name: "Sign out" }));
        expect(await screen.findByRole("alert")).toHaveTextContent("Something went wrong");
        expect(store.getState().session.status).toBe("authenticated");
    });
    it("redirects anonymous visitors away from the protected workspace", async () => {
        mockFetch(() => json({}, 401));
        const store = createAppStore();
        render(
            <Provider store={store}>
                <MemoryRouter initialEntries={["/app"]}>
                    <App />
                </MemoryRouter>
            </Provider>,
        );
        expect(await screen.findByRole("heading", { name: "Welcome back" })).toBeInTheDocument();
        expect(store.getState().session.status).toBe("anonymous");
    });
    it("refreshes once for concurrent expired requests and retries both", async () => {
        let refreshed = false;
        let refreshCount = 0;
        mockFetch(async (request) => {
            if (request.url.endsWith("/refresh")) {
                refreshCount++;
                await new Promise((resolve) => setTimeout(resolve, 20));
                refreshed = true;
                return json(user);
            }
            return refreshed ? json(user) : json({}, 401);
        });
        const store = createAppStore();
        const extra = baseApi.injectEndpoints({
            endpoints: (builder) => ({
                otherUser: builder.query<typeof user, void>({ query: () => "/other" }),
            }),
        });
        const first = store.dispatch(authApi.endpoints.currentUser.initiate());
        const second = store.dispatch(extra.endpoints.otherUser.initiate());
        await expect(first.unwrap()).resolves.toEqual(user);
        await expect(second.unwrap()).resolves.toEqual(user);
        expect(refreshCount).toBe(1);
        first.unsubscribe();
        second.unsubscribe();
        store.dispatch(baseApi.util.resetApiState());
    });
    it("ends the session and clears cached resources when refresh is rejected", async () => {
        const store = createAppStore();
        store.dispatch(sessionReceived(user));
        await store.dispatch(authApi.util.upsertQueryData("currentUser", undefined, user));
        mockFetch(() => json({}, 401));
        await store.dispatch(
            authApi.endpoints.currentUser.initiate(undefined, {
                forceRefetch: true,
                subscribe: false,
            }),
        );
        expect(store.getState().session.status).toBe("anonymous");
        expect(Object.keys(store.getState().api.queries)).toHaveLength(0);
    });
    it("keeps the session on transient refresh failure", async () => {
        mockFetch((request) => json({}, request.url.endsWith("/refresh") ? 503 : 401));
        const store = createAppStore();
        store.dispatch(sessionReceived(user));
        await store.dispatch(
            authApi.endpoints.currentUser.initiate(undefined, { subscribe: false }),
        );
        expect(store.getState().session.status).toBe("authenticated");
        store.dispatch(sessionEnded());
    });
});
