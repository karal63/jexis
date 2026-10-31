import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowRight, Eye, EyeOff } from "lucide-react";
import { AuthField, AuthForm, AuthLayout, AuthSubmit } from "@/features/auth/ui/AuthLayout";
import { api, getApiError } from "@/shared/config/api.ts";

export default function SignupPage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [passwordMismatch, setPasswordMismatch] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError("");
        const values = new FormData(event.currentTarget);
        if (values.get("password") !== values.get("confirmPassword")) {
            setPasswordMismatch(true);
            return;
        }
        setPasswordMismatch(false);
        setLoading(true);
        try {
            await api.post("/auth/signup", {
                firstName: values.get("firstName"),
                lastName: values.get("lastName"),
                email: values.get("email"),
                phoneNumber: values.get("phoneNumber") || null,
                password: values.get("password"),
            });
            navigate("/");
        } catch (requestError) {
            setError(getApiError(requestError));
        } finally {
            setLoading(false);
        }
    }

    return (
        <AuthLayout mode="signup">
            <AuthForm onSubmit={handleSubmit}>
                <div className="grid grid-cols-2 gap-3">
                    <AuthField
                        label="First name"
                        name="firstName"
                        autoComplete="given-name"
                        placeholder="Sam"
                    />
                    <AuthField
                        label="Last name"
                        name="lastName"
                        autoComplete="family-name"
                        placeholder="Taylor"
                    />
                </div>
                <AuthField
                    label="Work email"
                    name="email"
                    type="email"
                    autoComplete="email"
                    placeholder="you@company.com"
                />
                <AuthField
                    label="Phone number"
                    name="phoneNumber"
                    type="tel"
                    autoComplete="tel"
                    required={false}
                    placeholder="Optional"
                />
                <label className="block">
                    <span className="mb-1.5 block text-xs font-semibold text-foreground/90">
                        Password
                    </span>
                    <span className="relative block">
                        <input
                            name="password"
                            type={showPassword ? "text" : "password"}
                            autoComplete="new-password"
                            required
                            minLength={8}
                            maxLength={128}
                            className="h-11 w-full rounded-md border border-input bg-card px-3.5 pr-11 text-sm text-foreground outline-none transition placeholder:text-muted-foreground/70 focus:border-ring focus:ring-2 focus:ring-ring/15"
                            placeholder="At least 8 characters"
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute inset-y-0 right-0 grid w-11 place-items-center text-muted-foreground hover:text-brand-leaf"
                            aria-label={showPassword ? "Hide password" : "Show password"}
                        >
                            {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                        </button>
                    </span>
                    <span className="mt-1 block min-h-4 text-[10px] text-muted-foreground/80">
                        Use at least 8 characters.
                    </span>
                </label>
                <label className="block">
                    <span className="mb-1.5 block text-xs font-semibold text-foreground/90">
                        Confirm password
                    </span>
                    <span className="relative block">
                        <input
                            name="confirmPassword"
                            type={showPassword ? "text" : "password"}
                            autoComplete="new-password"
                            required
                            minLength={8}
                            maxLength={128}
                            aria-invalid={passwordMismatch}
                            className={`h-11 w-full rounded-md border bg-card px-3.5 pr-11 text-sm text-foreground outline-none transition placeholder:text-muted-foreground/70 focus:border-ring focus:ring-2 focus:ring-ring/15 ${passwordMismatch ? "border-danger" : "border-input"}`}
                            placeholder="Re-enter your password"
                        />
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            className="absolute inset-y-0 right-0 grid w-11 place-items-center text-muted-foreground hover:text-brand-leaf"
                            aria-label={showPassword ? "Hide password" : "Show password"}
                        >
                            {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                        </button>
                    </span>
                    <span
                        className={`mt-1 block min-h-4 text-[10px] ${passwordMismatch ? "text-danger" : "text-transparent"}`}
                        role={passwordMismatch ? "alert" : undefined}
                    >
                        {passwordMismatch ? "Passwords do not match." : " "}
                    </span>
                </label>
                {error && (
                    <p
                        role="alert"
                        className="mb-3 rounded-md border border-danger-line bg-danger-bg px-3 py-2.5 text-xs leading-5 text-danger"
                    >
                        {error}
                    </p>
                )}
                <AuthSubmit loading={loading}>
                    Create account <ArrowRight size={15} />
                </AuthSubmit>
                <p className="pt-4 text-center text-xs text-muted-foreground">
                    Already have an account?{" "}
                    <Link className="font-semibold text-brand-leaf hover:underline" to="/login">
                        Sign in
                    </Link>
                </p>
            </AuthForm>
        </AuthLayout>
    );
}
