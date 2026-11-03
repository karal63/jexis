import { useSignupMutation } from "../api/authApi";
import { toSignupRequest } from "../model/signup.schema";
import { PasswordInput } from "@/shared/ui/password-input";
import { signupSchema, type SignupValues } from "../model/signup.schema";
import { applyFormError } from "../model/applyFormError";
import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { FormField as AuthField } from "@/shared/ui/form-field";
import { AuthForm, AuthSubmit, FormError } from "./AuthForm";

export function SignupForm({ onSuccess }: { onSuccess: () => void }) {
    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm<SignupValues>({
        resolver: zodResolver(signupSchema),
        defaultValues: { phoneNumber: "" },
    });

    const [signup] = useSignupMutation();
    async function submit(values: SignupValues) {
        try {
            await signup(toSignupRequest(values)).unwrap();
            onSuccess();
        } catch (error) {
            applyFormError(error, setError, [
                "firstName",
                "lastName",
                "email",
                "phoneNumber",
                "password",
            ]);
        }
    }

    return (
        <>
            <AuthForm onSubmit={handleSubmit(submit)}>
                <div className="grid grid-cols-2 gap-3">
                    <AuthField
                        label="First name"
                        autoComplete="given-name"
                        placeholder="Sam"
                        error={errors.firstName?.message}
                        {...register("firstName")}
                        name="firstName"
                    />
                    <AuthField
                        label="Last name"
                        autoComplete="family-name"
                        placeholder="Taylor"
                        error={errors.lastName?.message}
                        {...register("lastName")}
                        name="lastName"
                    />
                </div>
                <AuthField
                    label="Work email"
                    type="email"
                    autoComplete="email"
                    placeholder="you@company.com"
                    error={errors.email?.message}
                    {...register("email")}
                    name="email"
                />
                <AuthField
                    label="Phone number"
                    error={errors.phoneNumber?.message}
                    type="tel"
                    autoComplete="tel"
                    required={false}
                    placeholder="Optional"
                    {...register("phoneNumber")}
                    name="phoneNumber"
                />
                <PasswordInput
                    label="Password"
                    autoComplete="new-password"
                    required
                    error={errors.password?.message}
                    {...register("password")}
                />
                <PasswordInput
                    label="Confirm password"
                    autoComplete="new-password"
                    required
                    error={errors.confirmPassword?.message}
                    {...register("confirmPassword")}
                />
                <FormError message={errors.root?.server?.message} />
                <AuthSubmit loading={isSubmitting}>
                    Create account <ArrowRight size={15} />
                </AuthSubmit>
                <p className="pt-4 text-center text-xs text-muted-foreground">
                    Already have an account?{" "}
                    <Link className="font-semibold text-brand-leaf hover:underline" to="/login">
                        Sign in
                    </Link>
                </p>
            </AuthForm>
        </>
    );
}
