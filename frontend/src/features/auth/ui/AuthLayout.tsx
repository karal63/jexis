import type { ReactNode } from "react";
import { CenteredPage } from "@/shared/ui/centered-page";
import { useI18n } from "@/shared/i18n/useI18n";

export function AuthLayout({
    children,
    mode,
}: {
    children: ReactNode;
    mode: "login" | "signup" | "forgot" | "reset";
}) {
    const { t } = useI18n();
    const title =
        mode === "login"
            ? t("auth.welcome")
            : mode === "signup"
              ? t("auth.createTitle")
              : t("auth.resetTitle");
    const description =
        mode === "login"
            ? t("auth.loginIntro")
            : mode === "signup"
              ? t("auth.signupIntro")
              : mode === "forgot"
                ? t("auth.forgotIntro")
                : t("auth.resetIntro");

    return (
        <CenteredPage
            title={title}
            description={description}
            backTo="/"
            backLabel={t("common.backToJexis")}
        >
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
        </CenteredPage>
    );
}
