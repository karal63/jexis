import { AuthLayout, ForgotPasswordForm } from "@/features/auth";
export default function ForgotPasswordPage() {
    return (
        <AuthLayout mode="forgot">
            <ForgotPasswordForm />
        </AuthLayout>
    );
}
