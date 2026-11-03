import { useNavigate } from "react-router-dom";
import { AuthLayout, LoginForm } from "@/features/auth";
export default function LoginPage() {
    const navigate = useNavigate();
    return (
        <AuthLayout mode="login">
            <LoginForm onSuccess={() => navigate("/app", { replace: true })} />
        </AuthLayout>
    );
}
