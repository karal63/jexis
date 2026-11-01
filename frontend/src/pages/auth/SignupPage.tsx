import { useNavigate } from "react-router-dom";
import { AuthLayout, SignupForm } from "@/features/auth";
export default function SignupPage() {
    const navigate = useNavigate();
    return (
        <AuthLayout mode="signup">
            <SignupForm onSuccess={() => navigate("/app", { replace: true })} />
        </AuthLayout>
    );
}
