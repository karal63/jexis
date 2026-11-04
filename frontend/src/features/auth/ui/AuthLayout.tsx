import type { ReactNode } from "react";
import { ArrowLeft, Leaf } from "lucide-react";
import { Link } from "react-router-dom";
import { useI18n } from "@/shared/i18n/useI18n";

export function AuthLayout({
    children,
    mode,
}: {
    children: ReactNode;
    mode: "login" | "signup" | "forgot" | "reset";
}) {
    const { t } = useI18n();
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
                            ? t("auth.welcome")
                            : mode === "signup"
                              ? t("auth.createTitle")
                              : t("auth.resetTitle")}
                    </h1>
                    <p className="mt-2 text-sm text-muted-foreground">
                        {mode === "login"
                            ? t("auth.loginIntro")
                            : mode === "signup"
                              ? t("auth.signupIntro")
                              : mode === "forgot"
                                ? t("auth.forgotIntro")
                                : t("auth.resetIntro")}
                    </p>
                </div>
                {children}
                <p className="mt-8 text-center text-[10px] leading-5 text-muted-foreground/80">
                    {t("auth.agreeBefore")}
                    <a
                        href="mailto:hello@jexis.com?subject=Terms%20of%20service"
                        className="underline decoration-border underline-offset-2"
                    >
                        {t("auth.terms")}
                    </a>
                    {t("auth.agreeAnd")}
                    <a
                        href="mailto:hello@jexis.com?subject=Privacy%20policy"
                        className="underline decoration-border underline-offset-2"
                    >
                        {t("auth.privacy")}
                    </a>
                    {t("auth.agreeEnd")}
                </p>
            </section>
            <Link
                to="/"
                className="mt-auto inline-flex items-center gap-1.5 text-[11px] text-muted-foreground hover:text-foreground"
            >
                <ArrowLeft size={13} /> {t("common.backToJexis")}
            </Link>
        </main>
    );
}
