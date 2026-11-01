import { lazy, Suspense } from "react";
import { Navigate, Outlet, Route, Routes } from "react-router-dom";
const LandingPage = lazy(() => import("@/pages/landing/LandingPage"));
const LoginPage = lazy(() => import("@/pages/auth/LoginPage"));
const SignupPage = lazy(() => import("@/pages/auth/SignupPage"));
const ForgotPasswordPage = lazy(() => import("@/pages/auth/ForgotPasswordPage"));
const HomePage = lazy(() => import("@/pages/home/HomePage"));
import { useAppSelector } from "./hooks";
import { Button } from "@/shared/ui/button";

function RequireSession() {
    const status = useAppSelector((state) => state.session.status);
    return status === "authenticated" ? <Outlet /> : <Navigate to="/login" replace />;
}

function GuestOnly() {
    const status = useAppSelector((state) => state.session.status);
    return status === "authenticated" ? <Navigate to="/app" replace /> : <Outlet />;
}

export default function Router() {
    return (
        <Suspense
            fallback={
                <p role="status" className="p-6">
                    Loading page…
                </p>
            }
        >
            <Routes>
                <Route path="/" element={<LandingPage />} />
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
                            <h1>Page not found</h1>
                            <Button to="/">Back to Jexis</Button>
                        </main>
                    }
                />
            </Routes>
        </Suspense>
    );
}
