import { useLoginMutation } from "../api/authApi";
import { PasswordInput } from "@/shared/ui/password-input";
import { loginSchema, type LoginValues } from "../model/login.schema";
import { applyFormError } from "../model/applyFormError";
import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { FormField as AuthField } from "@/shared/ui/form-field";
import { AuthForm, AuthSubmit, FormError } from "./AuthForm";

export function LoginForm({ onSuccess }: { onSuccess: () => void }) {
    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<LoginValues>({
        resolver: zodResolver(loginSchema),
    });

    const [login] = useLoginMutation();
    async function submit(values: LoginValues) {
        try {
            await login(values).unwrap();
            onSuccess();
        } catch (error) {
            applyFormError(error, setError, ["email", "password"]);
        }
    }

    return (
        <>
            <AuthForm onSubmit={handleSubmit(submit)}>
                <AuthField
                    label="Email address"
                    type="email"
                    autoComplete="email"
                    placeholder="you@company.com"
                    error={errors.email?.message}
                    {...register("email")}
                    name="email"
                />
                <PasswordInput
                    label="Password"
                    autoComplete="current-password"
                    required
                    error={errors.password?.message}
                    {...register("password")}
                />
                <div className="-mt-1 mb-2 flex justify-end">
                    <Link
                        className="text-[11px] font-semibold text-brand-leaf hover:underline"
                        to="/forgot-password"
                    >
                        Forgot password?
                    </Link>
                </div>
                <FormError message={errors.root?.server?.message} />
                <AuthSubmit loading={isSubmitting}>
                    Sign in <ArrowRight size={15} />
                </AuthSubmit>
                <p className="pt-5 text-center text-xs text-muted-foreground">
                    New to Jexis?{" "}
                    <Link className="font-semibold text-brand-leaf hover:underline" to="/signup">
                        Create an account
                    </Link>
                </p>
            </AuthForm>
        </>
    );
}
