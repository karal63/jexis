import { lazy, Suspense } from "react";
import { Navigate, Outlet, Route, Routes } from "react-router-dom";
const LandingPage = lazy(() => import("@/pages/landing/LandingPage"));
const LoginPage = lazy(() => import("@/pages/auth/LoginPage"));
const SignupPage = lazy(() => import("@/pages/auth/SignupPage"));
const ForgotPasswordPage = lazy(() => import("@/pages/auth/ForgotPasswordPage"));
const ResetPasswordPage = lazy(() => import("@/pages/auth/ResetPasswordPage"));
const AllAccountsPage = lazy(() => import("@/pages/accounts/AllAccountsPage"));
const AccountHome = lazy(() => import("@/app/routes/accounts/AccountHome"));
const AccountPage = lazy(() => import("@/pages/accounts/AccountPage"));
const AccountSettingsPage = lazy(() => import("@/pages/accounts/AccountSettingsPage"));
const AccountWorkspaceLayout = lazy(() => import("@/app/routes/accounts/AccountWorkspaceLayout"));
const SettingsPage = lazy(() => import("@/pages/settings/SettingsPage"));
import { ActivationNotice } from "@/features/auth/ui/ActivationNotice";
import { useAppSelector } from "./hooks";
import { Button } from "@/shared/ui/button";
import { PageLoader } from "@/shared/ui/page-loader";
import { useI18n } from "@/shared/i18n/useI18n";

function RequireSession() {
    const status = useAppSelector((state) => state.session.status);
    return status === "authenticated" ? <Outlet /> : <Navigate to="/login" replace />;
}

function AccountLayout() {
    const user = useAppSelector((state) => state.session.user);
    if (!user || user.isActivated) return <Outlet />;

    return (
        <div className="grid h-dvh grid-rows-[auto_minmax(0,1fr)]">
            <ActivationNotice className="border-b border-brand-leaf/30 bg-brand-leaf/15 px-5 py-2 text-accent-foreground sm:px-8" />
            <div className="overflow-y-auto [&>main]:min-h-full [&>[data-workspace-shell]]:h-full">
                <Outlet />
            </div>
        </div>
    );
}

function GuestOnly() {
    const status = useAppSelector((state) => state.session.status);
    return status === "authenticated" ? <Navigate to="/app" replace /> : <Outlet />;
}

export default function Router() {
    const { t } = useI18n();
    return (
        <Suspense fallback={<PageLoader label={t("route.loading")} />}>
            <Routes>
                <Route path="/" element={<LandingPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
                <Route element={<GuestOnly />}>
                    <Route path="/login" element={<LoginPage />} />
                    <Route path="/signup" element={<SignupPage />} />
                    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                </Route>
                <Route element={<RequireSession />}>
                    <Route element={<AccountLayout />}>
                        <Route path="/app" element={<AccountHome />} />
                        <Route path="/app/accounts" element={<AllAccountsPage />} />
                        <Route path="/app/accounts/:accountId" element={<AccountWorkspaceLayout />}>
                            <Route index element={<AccountPage />} />
                            <Route path="settings" element={<AccountSettingsPage />} />
                        </Route>
                        <Route
                            path="/app/accounts/:accountId/external-account"
                            element={<AccountPage bankAccountSetup />}
                        />
                        <Route
                            path="/app/accounts/:accountId/bank-accounts"
                            element={<Navigate to="../settings" relative="path" replace />}
                        />
                    </Route>
                    <Route path="/app/settings" element={<SettingsPage />} />
                </Route>
                <Route
                    path="*"
                    element={
                        <main className="grid min-h-screen place-content-center gap-4 p-6">
                            <h1>{t("route.notFound")}</h1>
                            <Button to="/">{t("common.backToJexis")}</Button>
                        </main>
                    }
                />
            </Routes>
        </Suspense>
    );
}
