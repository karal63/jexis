import { useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ArrowRight, Eye, EyeOff } from "lucide-react";
import { AuthField, AuthForm, AuthLayout, AuthSubmit } from "@/features/auth/ui/AuthLayout";
import { api, getApiError } from "@/shared/config/api.ts";

export default function LoginPage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [showPassword, setShowPassword] = useState(false);

    async function handleSubmit(event: FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError("");
        setLoading(true);
        const values = new FormData(event.currentTarget);
        try {
            await api.post("/auth/login", {
                email: values.get("email"),
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
        <AuthLayout mode="login">
            <AuthForm onSubmit={handleSubmit}>
                <AuthField
                    label="Email address"
                    name="email"
                    type="email"
                    autoComplete="email"
                    placeholder="you@company.com"
                />
                <label className="block">
                    <span className="mb-1.5 block text-xs font-semibold text-foreground/90">
                        Password
                    </span>
                    <span className="relative block">
                        <input
                            name="password"
                            type={showPassword ? "text" : "password"}
                            autoComplete="current-password"
                            required
                            minLength={6}
                            className="h-11 w-full rounded-md border border-input bg-card px-3.5 pr-11 text-sm text-foreground outline-none transition placeholder:text-muted-foreground/70 focus:border-ring focus:ring-2 focus:ring-ring/15"
                            placeholder="Your password"
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
                    <span className="mt-1 block min-h-4" />
                </label>
                <div className="-mt-1 mb-2 flex justify-end">
                    <Link
                        className="text-[11px] font-semibold text-brand-leaf hover:underline"
                        to="/forgot-password"
                    >
                        Forgot password?
                    </Link>
                </div>
                {error && (
                    <p
                        role="alert"
                        className="mb-3 rounded-md border border-danger-line bg-danger-bg px-3 py-2.5 text-xs leading-5 text-danger"
                    >
                        {error}
                    </p>
                )}
                <AuthSubmit loading={loading}>
                    Sign in <ArrowRight size={15} />
                </AuthSubmit>
                <p className="pt-5 text-center text-xs text-muted-foreground">
                    New to Jexis?{" "}
                    <Link className="font-semibold text-brand-leaf hover:underline" to="/signup">
                        Create an account
                    </Link>
                </p>
            </AuthForm>
        </AuthLayout>
    );
}
