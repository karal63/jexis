import type { ReactNode } from "react";
import { ArrowLeft, Leaf } from "lucide-react";
import { Link } from "react-router-dom";

export function AuthLayout({
    children,
    mode,
}: {
    children: ReactNode;
    mode: "login" | "signup" | "forgot";
}) {
    return (
        <main className="flex min-h-screen flex-col items-center bg-card px-5 pb-8 text-foreground">
            <header className="relative flex w-full max-w-5xl items-center justify-center pt-[6vh]">
                <Link
                    to="/"
                    className="inline-flex items-center gap-2 font-display text-2xl font-bold tracking-[-.08em] text-foreground"
                >
                    <span className="grid size-7 place-items-center text-foreground">
                        <Leaf size={18} strokeWidth={2.5} />
                    </span>
                    jexis
                </Link>
            </header>
            <section className="mx-auto mt-[clamp(56px,11vh,128px)] w-full max-w-[430px] pb-10">
                <div className="mb-8 text-center">
                    <h1 className="font-display text-[27px] font-bold tracking-[-.045em] text-foreground">
                        {mode === "login"
                            ? "Welcome back"
                            : mode === "signup"
                              ? "Create your account"
                              : "Reset your password"}
                    </h1>
                    <p className="mt-2 text-sm text-muted-foreground">
                        {mode === "login"
                            ? "Sign in to your Jexis account"
                            : mode === "signup"
                              ? "Start managing your business finances with Jexis"
                              : "Enter your email and we’ll send you a reset link."}
                    </p>
                </div>
                {children}
                <p className="mt-8 text-center text-[10px] leading-5 text-muted-foreground/80">
                    By continuing, you agree to Jexis’{" "}
                    <a
                        href="mailto:hello@jexis.com?subject=Terms%20of%20service"
                        className="underline decoration-border underline-offset-2"
                    >
                        Terms
                    </a>{" "}
                    and{" "}
                    <a
                        href="mailto:hello@jexis.com?subject=Privacy%20policy"
                        className="underline decoration-border underline-offset-2"
                    >
                        Privacy Policy
                    </a>
                    .
                </p>
            </section>
            <Link
                to="/"
                className="mt-auto inline-flex items-center gap-1.5 text-[11px] text-muted-foreground hover:text-foreground"
            >
                <ArrowLeft size={13} /> Back to Jexis
            </Link>
        </main>
    );
}
