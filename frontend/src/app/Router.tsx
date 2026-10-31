import { Route, Routes } from "react-router-dom";
import LandingPage from "@/pages/landing/LandingPage.tsx";
import LoginPage from "@/pages/auth/LoginPage.tsx";
import SignupPage from "@/pages/auth/SignupPage.tsx";
import ForgotPasswordPage from "@/pages/auth/ForgotPasswordPage.tsx";

export default function Router() {
    return (
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        </Routes>
    );
}
