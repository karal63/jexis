import { lazy, Suspense } from "react";
import { Navigate, Outlet, Route, Routes } from "react-router-dom";
const LandingPage = lazy(() => import("@/pages/landing/LandingPage"));
const LoginPage = lazy(() => import("@/pages/auth/LoginPage"));
const SignupPage = lazy(() => import("@/pages/auth/SignupPage"));
const ForgotPasswordPage = lazy(() => import("@/pages/auth/ForgotPasswordPage"));
const ResetPasswordPage = lazy(() => import("@/pages/auth/ResetPasswordPage"));
const HomePage = lazy(() => import("@/pages/home/HomePage"));
import { useAppSelector } from "./hooks";
import { Button } from "@/shared/ui/button";
import { PageLoader } from "@/shared/ui/page-loader";
import { useI18n } from "@/shared/i18n/useI18n";

function RequireSession() {
    const status = useAppSelector((state) => state.session.status);
    return status === "authenticated" ? <Outlet /> : <Navigate to="/login" replace />;
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
                    <Route path="/app" element={<HomePage />} />
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
